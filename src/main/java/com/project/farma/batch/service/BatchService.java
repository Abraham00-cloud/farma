package com.project.farma.batch.service;

import com.project.farma.batch.dto.*;
import com.project.farma.batch.mapper.BatchMapper;
import com.project.farma.batch.model.Batch;
import com.project.farma.batch.model.Status;
import com.project.farma.batch.repository.BatchRepository;
import com.project.farma.section.model.Section;
import com.project.farma.section.service.SectionService;
import com.project.farma.transaction.dto.InternalTransactionRequestDto;
import com.project.farma.transaction.model.TransactionCategory;
import com.project.farma.transaction.model.TransactionType;
import com.project.farma.transaction.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchService {
    private final SectionService sectionService;
    private final BatchRepository batchRepository;
    private final BatchMapper batchMapper;
    private final TransactionService transactionService;

    @Transactional
    public BatchResponseDto createBatch(BatchRequestDto requestDto) {
        Section section = sectionService.getSectionEntityById(requestDto.sectionId());
        handleBatchValidation(section, requestDto);

        Batch batch = batchMapper.toBatchEntity(requestDto);
        batch.setSection(section);
        batch.setStatus(Status.ACTIVE);
        batch.setCurrentCount(requestDto.initialCount());
        batch.setMortalityCount(0);
        batch.setExpectedEndDate(requestDto.expectedEndDate());
        batch.setBatchNumber(generateBatchNumber(section));

        sectionService.setSectionStatus(section.getId(), false);
        Batch savedBatch = batchRepository.save(batch);

        return batchMapper.toBatchResponseDto(savedBatch);
    }

    @Transactional
    public BatchCloseResponseDto closeBatch(Long batchId, BatchCloseRequestDto closeDto) {
        Batch batch = getBatchById(batchId);
        checkIfAlreadyCompleted(batch);

        batch.setStatus(Status.COMPLETED);
        batch.setActualEndDate(closeDto.actualEndDate());
        batchRepository.save(batch);

        sectionService.setSectionStatus(batch.getSection().getId(), true);

        logFinalHarvestTransaction(batch, closeDto);

        log.info("Batch [{}] successfully COMPLETED. Section [{}] is now unblocked and available.",
                batch.getBatchNumber(), batch.getSection().getName());

        return buildBatchCloseResponseDto(batch, closeDto);
    }

    @Transactional
    public void recordPartialSale(Long batchId, PartialSaleRequestDto requestDto) {
        Batch batch = getBatchById(batchId);
        checkIfAlreadyCompleted(batch);

        if (requestDto.birdsSold() > batch.getCurrentCount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot sell more birds than are currently alive in the pen.");
        }

        batch.setCurrentCount(batch.getCurrentCount() - requestDto.birdsSold());
        batchRepository.save(batch);

        logPartialSaleTransaction(batch, requestDto);

        log.info("Recorded partial sale for Batch [{}]. Sold {} birds. Remaining: {}",
                batch.getBatchNumber(), requestDto.birdsSold(), batch.getCurrentCount());
    }

    public void updateBatchMortality(Long batchId, Integer deathCount) {
        if (deathCount == null || deathCount == 0) return;

        Batch batch = getBatchById(batchId);
        batch.setMortalityCount(batch.getMortalityCount() + deathCount);
        batch.setCurrentCount(batch.getCurrentCount() - deathCount);

        batchRepository.save(batch);
    }

    public Batch getBatchById(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found"));
    }

    public BatchResponseDto getBatchDetailsById(Long id) {
        Batch batch = getBatchById(id);
        return batchMapper.toBatchResponseDto(batch);
    }

    public Page<BatchResponseDto> getBatchesByFarmId(Long farmId, Pageable pageable) {
        return batchRepository.findBySectionFarmId(farmId, pageable)
                .map(batchMapper::toBatchResponseDto);
    }

    public Page<BatchResponseDto> getBatchesBySectionId(Long sectionId, Pageable pageable) {
        return batchRepository.findBySectionId(sectionId, pageable)
                .map(batchMapper::toBatchResponseDto);
    }

    // INTERNAL SERVICE METHODS (For Analytics & Finance)

    public List<Batch> getBatchEntitiesByFarmId(Long farmId) {
        return batchRepository.findBySectionFarmId(farmId);
    }

    public List<Batch> getBatchEntitiesBySectionId(Long sectionId) {
        return batchRepository.findBySectionId(sectionId);
    }

    // PRIVATE HELPER METHODS

    private String generateBatchNumber(Section section) {
        return section.getName().toUpperCase().replace(" ", "") + "-" + System.currentTimeMillis();
    }

    private void handleBatchValidation(Section section, BatchRequestDto requestDto) {
        if (!section.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Farm section is currently not available");
        }
        if (requestDto.initialCount() > section.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Batch size exceeds the section capacity of " + section.getCapacity());
        }
    }

    private void checkIfAlreadyCompleted(Batch batch) {
        if (batch.getStatus() == Status.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Action blocked: Batch is already completed and closed.");
        }
    }

    private void logFinalHarvestTransaction(Batch batch, BatchCloseRequestDto closeDto) {
        if (closeDto.totalSaleRevenue() != null && closeDto.totalSaleRevenue() > 0) {
            Long organisationId = batch.getSection().getFarm().getOrganisation().getId();
            String harvestNotes = closeDto.harvestNotes() != null && !closeDto.harvestNotes().isBlank() ? closeDto.harvestNotes() : "N/A";

            String auditNarrative = String.format("Final Harvest Revenue: Sold %d birds from %s (Batch #%s). Notes: %s",
                    closeDto.totalBirdsSold(), batch.getSection().getName(), batch.getBatchNumber(), harvestNotes);

            transactionService.createInternalTransaction(new InternalTransactionRequestDto(
                    organisationId, batch.getId(), closeDto.totalSaleRevenue(),
                    TransactionType.CREDIT, TransactionCategory.LIVESTOCK_SALE, auditNarrative
            ));
        }
    }

    private void logPartialSaleTransaction(Batch batch, PartialSaleRequestDto requestDto) {
        if (requestDto.saleRevenue() != null && requestDto.saleRevenue() > 0) {
            Long organisationId = batch.getSection().getFarm().getOrganisation().getId();
            String notes = requestDto.notes() != null && !requestDto.notes().isBlank() ? requestDto.notes() : "N/A";

            String auditNarrative = String.format("Partial Sale: Sold %d birds from %s (Batch #%s). Notes: %s",
                    requestDto.birdsSold(), batch.getSection().getName(), batch.getBatchNumber(), notes);

            transactionService.createInternalTransaction(new InternalTransactionRequestDto(
                    organisationId, batch.getId(), requestDto.saleRevenue(),
                    TransactionType.CREDIT, TransactionCategory.LIVESTOCK_SALE, auditNarrative
            ));
        }
    }

    private BatchCloseResponseDto buildBatchCloseResponseDto(Batch batch, BatchCloseRequestDto closeDto) {
        return new BatchCloseResponseDto(
                batch.getId(),
                batch.getBatchNumber(),
                batch.getStatus(),
                batch.getStartDate(),
                batch.getActualEndDate(),
                batch.getCurrentCount(),
                closeDto.totalBirdsSold(),
                closeDto.totalSaleRevenue(),
                true,
                closeDto.harvestNotes()
        );
    }
}