package com.project.farma.dailyLogs.service;

import com.project.farma.batch.model.Batch;
import com.project.farma.batch.service.BatchService;
import com.project.farma.dailyLogs.dto.DailyLogRequestDto;
import com.project.farma.dailyLogs.dto.DailyLogResponseDto;
import com.project.farma.dailyLogs.mapper.DailyLogMapper;
import com.project.farma.dailyLogs.model.DailyLog;
import com.project.farma.dailyLogs.repository.DailyLogRepository;
import com.project.farma.inventory.model.Inventory;
import com.project.farma.inventory.service.InventoryService;
import com.project.farma.transaction.dto.InternalTransactionRequestDto;
import com.project.farma.transaction.model.TransactionCategory;
import com.project.farma.transaction.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DailyLogService {
    private final DailyLogMapper dailyLogMapper;
    private final BatchService batchService;
    private final DailyLogRepository dailyLogRepository;
    private final InventoryService inventoryService;
    private final TransactionService transactionService;

    @Transactional
    public DailyLogResponseDto createDailyLog(DailyLogRequestDto requestDto) {
        Batch batch = batchService.getBatchById(requestDto.batchId());

        handleDailyLogValidation(requestDto, batch);

        DailyLog dailyLog = dailyLogMapper.toDailyLogEntity(requestDto);
        dailyLog.setBatch(batch);
        dailyLog.setCreatedAt(LocalDateTime.now());

        handleFeedAndMedicineInventoryValidation(requestDto, dailyLog, batch);
        batchService.updateBatchMortality(requestDto.batchId(), requestDto.mortalityCount());

        DailyLog savedDailyLog = dailyLogRepository.save(dailyLog);
        return dailyLogMapper.toDailyLogResponseDto(savedDailyLog);

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
