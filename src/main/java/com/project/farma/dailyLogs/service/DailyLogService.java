package com.project.farma.dailyLogs.service;

import com.project.farma.analytics.alert.repository.SystemAlertRepository;
import com.project.farma.analytics.benchmark.model.WeeklyBreedStandard;
import com.project.farma.analytics.benchmark.service.AnalyticsHelperService;
import com.project.farma.analytics.weather.AutomatedWeatherService;
import com.project.farma.analytics.engine.DailyLogEvaluator;
import com.project.farma.batch.model.Batch;
import com.project.farma.batch.model.Status;
import com.project.farma.batch.service.BatchService;
import com.project.farma.dailyLogs.dto.DailyLogRequestDto;
import com.project.farma.dailyLogs.dto.DailyLogResponseDto;
import com.project.farma.dailyLogs.mapper.DailyLogMapper;
import com.project.farma.dailyLogs.model.DailyLog;
import com.project.farma.dailyLogs.repository.DailyLogRepository;
import com.project.farma.inventory.model.Inventory;
import com.project.farma.inventory.service.InventoryService;
import com.project.farma.security.FarmUserPrincipal;
import com.project.farma.transaction.dto.InternalTransactionRequestDto;
import com.project.farma.transaction.model.TransactionCategory;
import com.project.farma.transaction.model.TransactionType;
import com.project.farma.transaction.service.TransactionService;
import com.project.farma.user.model.User;
import com.project.farma.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyLogService {
    private final DailyLogMapper dailyLogMapper;
    private final BatchService batchService;
    private final DailyLogRepository dailyLogRepository;
    private final InventoryService inventoryService;
    private final TransactionService transactionService;
    private final UserService userService;
    private final AnalyticsHelperService analyticsHelperService;
    private final AutomatedWeatherService automatedWeatherService;
    private final List<DailyLogEvaluator> logEvaluators;
    private final SystemAlertRepository systemAlertRepository;

    @Transactional
    public DailyLogResponseDto createDailyLog(DailyLogRequestDto requestDto) {
        Batch batch = batchService.getBatchById(requestDto.batchId());
        User auditorUser = getAuthenticatedUser();

        checkDailyLogValidity(requestDto, batch);

        DailyLog dailyLog = dailyLogMapper.toDailyLogEntity(requestDto);
        dailyLog.setBatch(batch);
        dailyLog.setRecordedBy(auditorUser);

        handleFeedAndMedicineDeductions(requestDto, dailyLog, batch);
        handleProduceAddition(requestDto, dailyLog, batch);

        batchService.updateBatchMortality(requestDto.batchId(), requestDto.mortalityCount());

        DailyLog savedDailyLog = dailyLogRepository.save(dailyLog);
        runLogAnalyticsEngine(savedDailyLog, batch);

        return dailyLogMapper.toDailyLogResponseDto(savedDailyLog);
    }

    @Transactional
    public void deleteDailyLog(Long logId) {
        DailyLog log = getDailyLogEntityById(logId);
        Long organisationId = log.getBatch().getSection().getFarm().getOrganisation().getId();

        revertMortalityIfNecessary(log);
        revertFeedInventoryAndRefund(log, organisationId);
        revertMedicineInventoryAndRefund(log, organisationId);
        revertProduceInventory(log);

        systemAlertRepository.deleteAllByDailyLogId(logId);
        dailyLogRepository.delete(log);
    }

    @Transactional
    public DailyLogResponseDto updateDailyLog(Long logId, DailyLogRequestDto requestDto) {
        DailyLog existingLog = getDailyLogEntityById(logId);
        Batch batch = existingLog.getBatch();
        Long organisationId = batch.getSection().getFarm().getOrganisation().getId();

        adjustMortalityDelta(existingLog, requestDto, batch);

        revertFeedInventoryAndRefund(existingLog, organisationId);
        revertMedicineInventoryAndRefund(existingLog, organisationId);
        revertProduceInventory(existingLog);

        handleFeedAndMedicineDeductions(requestDto, existingLog, batch);
        handleProduceAddition(requestDto, existingLog, batch);

        updateBasicLogFields(existingLog, requestDto);

        systemAlertRepository.deleteAllByDailyLogId(logId);
        DailyLog savedLog = dailyLogRepository.save(existingLog);

        runLogAnalyticsEngine(savedLog, batch);

        return dailyLogMapper.toDailyLogResponseDto(savedLog);
    }

    public Page<DailyLogResponseDto> getLogsForBatch(Long batchId, Pageable pageable) {
        return dailyLogRepository.findByBatchId(batchId, pageable)
                .map(dailyLogMapper::toDailyLogResponseDto);
    }

    public Page<DailyLogResponseDto> getLogsForBatchInWindow(Long batchId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return dailyLogRepository.findByBatchIdAndLogDateBetween(batchId, startDate, endDate, pageable)
                .map(dailyLogMapper::toDailyLogResponseDto);
    }

    @Async
    public void runLogAnalyticsEngine(DailyLog savedLog, Batch activeBatch) {
        log.info("Triggering Analytics Engine for Batch: {}", activeBatch.getBatchNumber());

        WeeklyBreedStandard biologicalTarget = null;
        Map<String, Double> climateSnapshot = null;

        try {
            biologicalTarget = analyticsHelperService.getTargetForBatchAtAge(activeBatch, savedLog.getLogDate());
        } catch (Exception e) {
            log.warn("Could not fetch biological target. Evaluators will run without it. Reason: {}", e.getMessage());
        }

        try {
            climateSnapshot = automatedWeatherService.fetchEnvironmentalSnapshot(activeBatch.getSection().getFarm());
        } catch (Exception e) {
            log.warn("Could not fetch climate snapshot. Evaluators will run without it. Reason: {}", e.getMessage());
        }

        if (logEvaluators == null || logEvaluators.isEmpty()) {
            log.warn("No DailyLogEvaluator plugins found! Alerts cannot be generated.");
            return;
        }

        for (DailyLogEvaluator evaluator : logEvaluators) {
            try {
                evaluator.evaluate(savedLog, activeBatch, biologicalTarget, climateSnapshot);
            } catch (Exception pluginException) {
                log.error("Analytical component [{}] encountered an error. Isolation safe. Root: {}",
                        evaluator.getClass().getSimpleName(), pluginException.getMessage());
            }
        }
    }

    public List<DailyLog> getLogEntitiesForBatch(Long batchId) {
        return dailyLogRepository.findByBatchId(batchId, Sort.by("logDate").ascending());
    }

    public List<DailyLog> getLogEntitiesForBatchInWindow(Long batchId, LocalDate startDate, LocalDate endDate) {
        return dailyLogRepository.findByBatchIdAndLogDateBetween(
                batchId, startDate, endDate, Sort.by("logDate").ascending()
        );
    }

    private DailyLog getDailyLogEntityById(Long logId) {
        return dailyLogRepository.findById(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Daily log not found"));
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof FarmUserPrincipal farmUser) {
            return userService.findById(farmUser.getId());
        }
        throw new IllegalStateException("Authentication principal is missing or malformed");
    }

    private void checkDailyLogValidity(DailyLogRequestDto requestDto, Batch batch) {
        if (batch.getStatus() == Status.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot record daily logs for a batch that is already completed.");
        }

        if (dailyLogRepository.existsByBatchIdAndLogDate(requestDto.batchId(), requestDto.logDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A log already exists for this batch on this date");
        }

        if (requestDto.mortalityCount() != null && (requestDto.mortalityCount() > batch.getCurrentCount() || requestDto.mortalityCount() < 0)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mortality cannot exceed the current batch population or be less than zero");
        }

        if (requestDto.feedQuantityUsed() != null && requestDto.feedQuantityUsed() > (batch.getCurrentCount() * 10)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Fat-Finger Alert: The logged feed quantity (%.2f) seems unusually high for a batch of %d birds. Please verify your units.",
                            requestDto.feedQuantityUsed(), batch.getCurrentCount()));
        }
    }

    private void handleProduceAddition(DailyLogRequestDto requestDto, DailyLog dailyLog, Batch batch) {
        if (requestDto.eggsCollected() != null && requestDto.eggsCollected() > 0) {
            Inventory eggInventory = inventoryService.getOrCreateEggInventory(batch.getSection().getFarm());
            inventoryService.updateStockLevel(eggInventory.getId(), (double) requestDto.eggsCollected());
            dailyLog.setEggsCollected(requestDto.eggsCollected());

            log.info("Auto-Stocked {} eggs into Inventory ID: {} for Farm: {}",
                    requestDto.eggsCollected(), eggInventory.getId(), batch.getSection().getFarm().getName());
        }
    }

    private void revertProduceInventory(DailyLog log) {
        if (log.getEggsCollected() != null && log.getEggsCollected() > 0) {
            Inventory eggInventory = inventoryService.getOrCreateEggInventory(log.getBatch().getSection().getFarm());
            inventoryService.updateStockLevel(eggInventory.getId(), -(double) log.getEggsCollected());
            log.setEggsCollected(0);
        }
    }

    private void handleFeedAndMedicineDeductions(DailyLogRequestDto requestDto, DailyLog dailyLog, Batch batch) {
        Long organisationId = batch.getSection().getFarm().getOrganisation().getId();
        Long farmId = batch.getSection().getFarm().getId();

        if (requestDto.feedInventoryId() != null && requestDto.feedQuantityUsed() != null && requestDto.feedQuantityUsed() > 0) {
            Inventory feedItem = inventoryService.getInventoryEntityById(requestDto.feedInventoryId());
            checkInventoryLocationMatch(feedItem, farmId, "feed");

            inventoryService.updateStockLevel(requestDto.feedInventoryId(), -requestDto.feedQuantityUsed());
            dailyLog.setFeedInventory(feedItem);

            Double feedCost = feedItem.getUnitPrice() * requestDto.feedQuantityUsed();
            dailyLog.setHistoricalFeedCost(feedCost);

            String feedDescription = String.format("Consumed %.2f of %s for Batch %s", requestDto.feedQuantityUsed(), feedItem.getName(), batch.getBatchNumber());
            transactionService.createInternalTransaction(new InternalTransactionRequestDto(
                    organisationId, batch.getId(), farmId, feedCost, TransactionType.DEBIT, TransactionCategory.FEED_CONSUMPTION, feedDescription
            ));
        }

        if (requestDto.medicineInventoryId() != null && requestDto.medicineQuantityUsed() != null && requestDto.medicineQuantityUsed() > 0) {
            Inventory medicineItem = inventoryService.getInventoryEntityById(requestDto.medicineInventoryId());
            checkInventoryLocationMatch(medicineItem, farmId, "medicine");

            inventoryService.updateStockLevel(requestDto.medicineInventoryId(), -requestDto.medicineQuantityUsed());
            dailyLog.setMedicineInventory(medicineItem);

            Double medicineCost = medicineItem.getUnitPrice() * requestDto.medicineQuantityUsed();
            dailyLog.setHistoricalMedicineCost(medicineCost);

            String medicineDescription = String.format("Administered %.2f of %s to Batch %s", requestDto.medicineQuantityUsed(), medicineItem.getName(), batch.getBatchNumber());
            transactionService.createInternalTransaction(new InternalTransactionRequestDto(
                    organisationId, batch.getId(), farmId, medicineCost, TransactionType.DEBIT, TransactionCategory.MEDICINE_CONSUMPTION, medicineDescription
            ));
        }
    }

    private void checkInventoryLocationMatch(Inventory item, Long farmId, String type) {
        if (!item.getFarm().getId().equals(farmId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Security Alert: Cannot consume " + type + " inventory assigned to a different farm facility.");
        }
    }

    private void revertMortalityIfNecessary(DailyLog log) {
        if (log.getMortalityCount() != null && log.getMortalityCount() > 0) {
            batchService.updateBatchMortality(log.getBatch().getId(), -log.getMortalityCount());
        }
    }

    private void adjustMortalityDelta(DailyLog existingLog, DailyLogRequestDto requestDto, Batch batch) {
        int oldMortality = existingLog.getMortalityCount() != null ? existingLog.getMortalityCount() : 0;
        int newMortality = requestDto.mortalityCount() != null ? requestDto.mortalityCount() : 0;
        int mortalityDelta = newMortality - oldMortality;

        if (mortalityDelta != 0) {
            if (mortalityDelta > batch.getCurrentCount()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Mortality update exceeds current live population.");
            }
            batchService.updateBatchMortality(batch.getId(), mortalityDelta);
        }
        existingLog.setMortalityCount(newMortality);
    }

    private void revertFeedInventoryAndRefund(DailyLog log, Long organisationId) {
        if (log.getFeedInventory() != null && log.getFeedQuantityUsed() != null) {
            inventoryService.updateStockLevel(log.getFeedInventory().getId(), log.getFeedQuantityUsed());
            Double feedRefund = log.getHistoricalFeedCost() != null ? log.getHistoricalFeedCost() : 0.0;

            if (feedRefund > 0) {
                Long farmId = log.getBatch().getSection().getFarm().getId();
                transactionService.createInternalTransaction(new InternalTransactionRequestDto(
                        organisationId, log.getBatch().getId(), farmId, feedRefund, TransactionType.CREDIT, TransactionCategory.OTHER_INCOME, "AUTO-REVERSAL: Refunded feed cost."
                ));
            }
            log.setFeedInventory(null);
            log.setFeedQuantityUsed(null);
            log.setHistoricalFeedCost(null);
        }
    }

    private void revertMedicineInventoryAndRefund(DailyLog log, Long organisationId) {
        if (log.getMedicineInventory() != null && log.getMedicineQuantityUsed() != null) {
            inventoryService.updateStockLevel(log.getMedicineInventory().getId(), log.getMedicineQuantityUsed());
            Double medicineRefund = log.getHistoricalMedicineCost() != null ? log.getHistoricalMedicineCost() : 0.0;

            if (medicineRefund > 0) {
                Long farmId = log.getBatch().getSection().getFarm().getId();
                transactionService.createInternalTransaction(new InternalTransactionRequestDto(
                        organisationId, log.getBatch().getId(), farmId, medicineRefund, TransactionType.CREDIT, TransactionCategory.OTHER_INCOME, "AUTO-REVERSAL: Refunded medicine cost."
                ));
            }
            log.setMedicineInventory(null);
            log.setMedicineQuantityUsed(null);
            log.setHistoricalMedicineCost(null);
        }
    }

    private void updateBasicLogFields(DailyLog existingLog, DailyLogRequestDto requestDto) {
        existingLog.setLogDate(requestDto.logDate());
        existingLog.setAverageWeight(requestDto.averageWeight());
        existingLog.setObservations(requestDto.observations());
        existingLog.setAdministrationMethod(requestDto.administrationMethod());
        existingLog.setFeedQuantityUsed(requestDto.feedQuantityUsed());
        existingLog.setMedicineQuantityUsed(requestDto.medicineQuantityUsed());
        existingLog.setEggsCollected(requestDto.eggsCollected() != null ? requestDto.eggsCollected() : 0);
    }
}