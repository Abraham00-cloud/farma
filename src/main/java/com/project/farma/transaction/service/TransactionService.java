package com.project.farma.transaction.service;

import com.project.farma.batch.model.Batch;
import com.project.farma.batch.model.Status;
import com.project.farma.batch.service.BatchService;
import com.project.farma.organisation.model.Organisation;
import com.project.farma.organisation.service.OrganisationService;
import com.project.farma.transaction.dto.InternalTransactionRequestDto;
import com.project.farma.transaction.dto.TransactionRequestDto;
import com.project.farma.transaction.dto.TransactionResponseDto;
import com.project.farma.transaction.mapper.TransactionMapper;
import com.project.farma.transaction.model.Transaction;
import com.project.farma.transaction.model.TransactionType;
import com.project.farma.transaction.repostiory.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final OrganisationService organisationService;
    private final BatchService batchService;
    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionResponseDto createTransaction(TransactionRequestDto requestDto) {
        Organisation organisation = organisationService.findById(requestDto.organisationId());

        Batch batch = null;
        if (requestDto.batchId() != null) {
            batch = batchService.getBatchById(requestDto.batchId());
            if (batch.getStatus() == Status.COMPLETED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot log transactions against a completed batch.");
            }
        }
        Transaction transaction = transactionMapper.toTransactionEntity(requestDto);
        transaction.setOrganisation(organisation);
        transaction.setBatch(batch);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toTransactionResponseDto(savedTransaction);
    }

    @Transactional
    public void createInternalTransaction(InternalTransactionRequestDto requestDto) {
        Organisation organisation = organisationService.findById(requestDto.organisationId());
        Batch batch = batchService.getBatchById(requestDto.batchId());

        Transaction internalTransaction = Transaction.builder()
                .amount(requestDto.amount())
                .type(TransactionType.DEBIT)
                .category(requestDto.category())
                .transactionDate(LocalDate.now())
                .description(requestDto.description())
                .isCashFlow(false)
                .organisation(organisation)
                .batch(batch)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(internalTransaction);
    }


    public List<TransactionResponseDto> getCompanyCashFlow(Long organisationId) {
        return transactionRepository.findByOrganisationIdAndIsCashFlowTrueOrderByTransactionDateDesc(organisationId)
                .stream()
                .map(transactionMapper::toTransactionResponseDto)
                .toList();
    }

    public List<TransactionResponseDto> getBatchLedger(Long batchId, Long organisationId) {
        validateBatchOwnership(batchId, organisationId);

        return transactionRepository.findByBatchIdAndOrganisationIdOrderByTransactionDateDesc(batchId, organisationId)
                .stream()
                .map(transactionMapper::toTransactionResponseDto)
                .toList();
    }

    public Double calculateLiveBatchProfitAndLoss(Long batchId, Long organisationId) {
        validateBatchOwnership(batchId, organisationId);

        Double netProfit = transactionRepository.calculateNetProfitsByBatchAndOrganisation(batchId, organisationId);
        return netProfit != null ? netProfit : 0.0;
    }

    public List<TransactionResponseDto> getFarmTransaction(Long farmId, Long organisationId) {
        return transactionRepository.findByFarmIdAndOrganisationId(farmId, organisationId)
                .stream()
                .map(transactionMapper::toTransactionResponseDto)
                .toList();
    }

    public Double calculateFarmProfitAndLoss(Long farmId, Long organisationId) {
        Double farmNetProfit = transactionRepository.calculateNetProfitByFarm(farmId, organisationId);
        return farmNetProfit != null ? farmNetProfit : 0.0;

    }

    private void validateBatchOwnership(Long batchId, Long organisationId) {
        Batch batch = batchService.getBatchById(batchId);
        Long ownerId = batch.getSection().getFarm().getOrganisation().getId();

        if (!ownerId.equals(organisationId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You do not own this batch or farm asset.");
        }
    }
}

