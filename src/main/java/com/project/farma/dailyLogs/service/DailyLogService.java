package com.project.farma.dailyLogs.service;

import com.project.farma.analytics.weeklyBreedStandard.model.WeeklyBreedStandard;
import com.project.farma.analytics.weeklyBreedStandard.service.AnalyticsHelperService;
import com.project.farma.analytics.weeklyBreedStandard.service.AutomatedWeatherService;
import com.project.farma.analytics.weeklyBreedStandard.service.DailyLogEvaluator;
import com.project.farma.batch.model.Batch;
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
import com.project.farma.transaction.service.TransactionService;
import com.project.farma.user.model.User;
import com.project.farma.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Transactional
    public DailyLogResponseDto createDailyLog(DailyLogRequestDto requestDto) {
        Batch batch = batchService.getBatchById(requestDto.batchId());

        Object pricipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId;

        if (pricipal instanceof FarmUserPrincipal farmUser) {
            currentUserId = farmUser.getId();
        } else {
            throw new IllegalStateException("Authentication principal is missing or malformed");
        }

        handleDailyLogValidation(requestDto, batch);

        DailyLog dailyLog = dailyLogMapper.toDailyLogEntity(requestDto);
        dailyLog.setBatch(batch);
        dailyLog.setCreatedAt(LocalDateTime.now());

        User auditorUser = userService.findById(currentUserId);
        dailyLog.setRecordedBy(auditorUser);


        handleFeedAndMedicineInventoryValidation(requestDto, dailyLog, batch);
        batchService.updateBatchMortality(requestDto.batchId(), requestDto.mortalityCount());

        DailyLog savedDailyLog = dailyLogRepository.save(dailyLog);

        runLogAnalyticsEngine(savedDailyLog, batch);
        return dailyLogMapper.toDailyLogResponseDto(savedDailyLog);

    }

    public void runLogAnalyticsEngine(DailyLog savedLog, Batch activeBatch) {
        try {
            // 1. Fetch historical breed benchmarks
            WeeklyBreedStandard biologicalTarget = analyticsHelperService.getTargetForBatchAtAge(activeBatch, savedLog.getLogDate());

            // 2. Automated data harvest (Zero worker data-entry overhead)
            Map<String, Double> climateSnapshot = automatedWeatherService.fetchEnvironmentalSnapshot(activeBatch.getSection().getFarm());

            // 3. Iterative Execution Loop: Process calculations through completely isolated pipelines
            for (DailyLogEvaluator evaluator : logEvaluators) {
                try {
                    evaluator.evaluate(savedLog, activeBatch, biologicalTarget, climateSnapshot);
                } catch (Exception pluginException) {
                    // Individual safety net: If a new formula contains an error, it is contained here
                    log.error("Analytical component [{}] encountered an error. Isolation safe. Root: {}",
                            evaluator.getClass().getSimpleName(), pluginException.getMessage());
                }
            }
        } catch (Exception globalEngineException) {
            // Master safe-guard: Ensures analytical calculations never block or crash core operational logging
            log.warn("Analytics engine execution bypassed for this log cycle: {}", globalEngineException.getMessage());
        }
    }

    public List<DailyLog> getLogsForBatch(Long batchId) {
        return dailyLogRepository.findByBatchIdOrderByLogDateAsc(batchId);
    }

    public List<DailyLog> getLogsForBatchInWindow(Long batchId, LocalDate startDate, LocalDate endDate) {
        return dailyLogRepository.findByBatchIdAndLogDateBetweenOrderByLogDateAsc(batchId, startDate, endDate);
    }

    private void handleFeedAndMedicineInventoryValidation(DailyLogRequestDto requestDto, DailyLog dailyLog, Batch batch) {
        Long organisationId = batch.getSection().getFarm().getOrganisation().getId();

        if (requestDto.feedInventoryId() != null && requestDto.feedQuantityUsed() != null && requestDto.feedQuantityUsed() > 0) {
            inventoryService.updateStockLevel(requestDto.feedInventoryId(), -requestDto.feedQuantityUsed());
            Inventory feedItem = inventoryService.getInventoryEntityById(requestDto.feedInventoryId());
            dailyLog.setFeedInventory(feedItem);

            Double feedCost = feedItem.getUnitPrice() * requestDto.feedQuantityUsed();
            String feedDescription = String.format("Consumed %.2f of %s for Batch %s",
                    requestDto.feedQuantityUsed(), feedItem.getName(), batch.getBatchNumber());

            transactionService.createInternalTransaction(new InternalTransactionRequestDto(
                    organisationId,
                    batch.getId(),
                    feedCost,
                    TransactionCategory.FEED_CONSUMPTION,
                    feedDescription
            ));
        }


        if (requestDto.medicineInventoryId() != null && requestDto.medicineQuantityUsed() != null && requestDto.medicineQuantityUsed() > 0) {
            inventoryService.updateStockLevel(requestDto.medicineInventoryId(), -requestDto.medicineQuantityUsed());
            Inventory medicineItem = inventoryService.getInventoryEntityById(requestDto.medicineInventoryId());
            dailyLog.setMedicineInventory(medicineItem);

            Double medicineCost = medicineItem.getUnitPrice() * requestDto.medicineQuantityUsed();
            String medicineDescription = String.format("Administered %.2f of %s to Batch %s", requestDto.medicineQuantityUsed(), medicineItem.getName(), batch.getBatchNumber());

            transactionService.createInternalTransaction(new InternalTransactionRequestDto(
                    organisationId,
                    batch.getId(),
                    medicineCost,
                    TransactionCategory.MEDICINE_CONSUMPTION,
                    medicineDescription
            ));
        }

    }


    private void handleDailyLogValidation(DailyLogRequestDto requestDto, Batch batch) {
        if ( dailyLogRepository.existsByBatchIdAndLogDate(requestDto.batchId(), requestDto.logDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A log already exists for this batch on this date");
        }

        if (requestDto.mortalityCount() != null && requestDto.mortalityCount() > batch.getCurrentCount() || requestDto.mortalityCount() < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mortality cannot exceed the current batch population or less than zero");
        }

    }
}
