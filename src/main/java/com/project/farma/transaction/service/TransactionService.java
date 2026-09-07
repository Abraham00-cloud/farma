package com.project.farma.transaction.service;

import com.project.farma.batch.model.Batch;
import com.project.farma.batch.model.Status;
import com.project.farma.batch.repository.BatchRepository;
import com.project.farma.farm.model.Farm;
import com.project.farma.farm.service.FarmService;
import com.project.farma.organisation.model.Organisation;
import com.project.farma.organisation.service.OrganisationService;
import com.project.farma.security.TenantContext;
import com.project.farma.transaction.dto.InternalTransactionRequestDto;
import com.project.farma.transaction.dto.TransactionRequestDto;
import com.project.farma.transaction.dto.TransactionResponseDto;
import com.project.farma.transaction.mapper.TransactionMapper;
import com.project.farma.transaction.model.Transaction;
import com.project.farma.transaction.model.TransactionType;
import com.project.farma.transaction.repostiory.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final BatchRepository batchRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;
    private final FarmService farmService;


    @Transactional
    public TransactionResponseDto createTransaction(TransactionRequestDto requestDto) {
        Organisation organisation = organisationService.findById(requestDto.organisationId());

        Transaction transaction = transactionMapper.toTransactionEntity(requestDto);
        transaction.setOrganisation(organisation);

        linkTransactionToAsset(transaction, requestDto);

        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toTransactionResponseDto(savedTransaction);
    }

    @Transactional
    public void createInternalTransaction(InternalTransactionRequestDto requestDto) {
        Organisation organisation = organisationService.findById(requestDto.organisationId());
        Batch batch = batchRepository.findById(requestDto.batchId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch not found"));

        Transaction internalTransaction = Transaction.builder()
                .amount(requestDto.amount())
                .type(requestDto.type())
                .category(requestDto.category())
                .transactionDate(LocalDate.now())
                .description(requestDto.description())
                .isCashFlow(false)
                .organisation(organisation)
                .batch(batch)
                .farm(batch.getSection().getFarm())
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(internalTransaction);
    }

    public Page<TransactionResponseDto> getAllTransactionsByOrganisation(Long organisationId, Pageable pageable) {
        return transactionRepository.findByOrganisationId(organisationId, pageable)
                .map(transactionMapper::toTransactionResponseDto);
    }

    public Page<TransactionResponseDto> getCompanyCashFlow(Long organisationId, Pageable pageable) {
        return transactionRepository.findByOrganisationIdAndIsCashFlowTrue(organisationId, pageable)
                .map(transactionMapper::toTransactionResponseDto);
    }

    public Page<TransactionResponseDto> getBatchTransactions(Long batchId, Long organisationId, Pageable pageable) {
        validateBatchOwnership(batchId, organisationId);
        return transactionRepository.findByBatchIdAndOrganisationId(batchId, organisationId, pageable)
                .map(transactionMapper::toTransactionResponseDto);
    }

    public Page<TransactionResponseDto> getFarmTransaction(Long farmId, Long organisationId, Pageable pageable) {
        return transactionRepository.findByFarmIdAndOrganisationId(farmId, organisationId, pageable)
                .map(transactionMapper::toTransactionResponseDto);
    }


    // INTERNAL & P&L CALCULATIONS (Flat Lists)

    public List<Transaction> getRawTransactionsForFarm(Long farmId) {
        Long organisationId = TenantContext.getTenantId();
        return transactionRepository.findByFarmIdAndOrganisationId(farmId, organisationId);
    }

    public List<Transaction> getRawTransactionsForBatch(Long batchId) {
        Long organisationId = TenantContext.getTenantId();
        validateBatchOwnership(batchId, organisationId);
        return transactionRepository.findByBatchIdAndOrganisationIdOrderByTransactionDateDesc(batchId, organisationId);
    }

    public Double calculateLiveBatchProfitAndLoss(Long batchId, Long organisationId) {
        validateBatchOwnership(batchId, organisationId);
        Double netProfit = transactionRepository.calculateNetProfitsByBatchAndOrganisation(batchId, organisationId);
        return netProfit != null ? netProfit : 0.0;
    }

    public Double calculateFarmProfitAndLoss(Long farmId, Long organisationId) {
        Double farmNetProfit = transactionRepository.calculateNetProfitByFarm(farmId, organisationId);
        return farmNetProfit != null ? farmNetProfit : 0.0;
    }

    public byte[] exportTransactionsToCsv(Long organisationId, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = transactionRepository.findTransactionsForAudit(organisationId, startDate, endDate);
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Transaction ID,Date,Type,Category,Amount,Cash Flow,Narrative\n");

        for (Transaction tx : transactions) {
            String description = tx.getDescription() != null
                    ? tx.getDescription().replace("\"", "\"\"")
                    : "";

            csvBuilder.append(tx.getId()).append(",")
                    .append(tx.getTransactionDate()).append(",")
                    .append(tx.getType()).append(",")
                    .append(tx.getCategory()).append(",")
                    .append(tx.getAmount()).append(",")
                    .append(tx.isCashFlow() ? "YES" : "NO").append(",")
                    .append("\"").append(description).append("\"\n");
        }
        return csvBuilder.toString().getBytes();
    }

    // PRIVATE HELPER METHODS


    private void linkTransactionToAsset(Transaction transaction, TransactionRequestDto requestDto) {
        if (requestDto.batchId() != null) {
            Batch batch = batchRepository.findById(requestDto.batchId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch not found"));

            if (batch.getStatus() == Status.COMPLETED) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot log transactions against a completed batch.");
            }
            transaction.setBatch(batch);
            transaction.setFarm(batch.getSection().getFarm());
        } else if (requestDto.farmId() != null) {
            Farm farm = farmService.getFarmById(requestDto.farmId());
            transaction.setFarm(farm);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction must be linked to either a Batch or a Farm.");
        }
    }

    private void validateBatchOwnership(Long batchId, Long organisationId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Batch not found"));

        Long ownerId = batch.getSection().getFarm().getOrganisation().getId();
        if (!ownerId.equals(organisationId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: You do not own this batch or farm asset.");
        }
    }
}