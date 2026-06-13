package com.project.farma.transaction.controller;

import com.project.farma.transaction.dto.TransactionRequestDto;
import com.project.farma.transaction.dto.TransactionResponseDto;
import com.project.farma.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
@Tag(
        name = "9. Financial & Transaction Management",
        description = "Enterprise auditing endpoints tracking cash-flow vectors, deep batch ledgers, and dynamic P&L calculations"
)
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasRole('PROPRIETOR')")
    @Operation(
            summary = "Record External Cash Flow Transaction",
            description = "Logs manual revenue (e.g., selling harvested livestock) or actual capital expenses against the enterprise tenant."
    )
    public ResponseEntity<TransactionResponseDto> createTransaction(@Valid @RequestBody TransactionRequestDto requestDto) {
        TransactionResponseDto transaction = transactionService.createTransaction(requestDto);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @GetMapping("/cash-flow/organisation/{organisationId}")
    @PreAuthorize("hasRole('PROPRIETOR')")
    @Operation(
            summary = "Fetch Company Cash Flow Statement",
            description = "Compiles a clean historical record of external income and expenses directly touching liquid asset reserves."
    )
    public ResponseEntity<List<TransactionResponseDto>> getCompanyCashFlow(@PathVariable Long organisationId) {
        List<TransactionResponseDto> cashFlow = transactionService.getCompanyCashFlow(organisationId);
        return new ResponseEntity<>(cashFlow, HttpStatus.OK);
    }

    @GetMapping("/ledger/batch/{batchId}/organisation/{organisationId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Fetch Detailed Batch Ledger",
            description = "Compiles every internal consumption and external revenue record linked to an isolated animal lifecycle cohort."
    )
    public ResponseEntity<List<TransactionResponseDto>> getBatchLedger(
            @PathVariable Long batchId,
            @PathVariable Long organisationId
    ) {
        List<TransactionResponseDto> ledger = transactionService.getBatchLedger(batchId, organisationId);
        return new ResponseEntity<>(ledger, HttpStatus.OK);
    }

    @GetMapping("/pnl/batch/{batchId}/organisation/{organisationId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Calculate Live Batch Profit & Loss",
            description = "Dynamically nets out operational inputs against cohort revenue outputs to determine the current net margins for an active/historical batch."
    )
    public ResponseEntity<Double> calculateLiveBatchProfitAndLoss(
            @PathVariable Long batchId,
            @PathVariable Long organisationId
    ) {
        Double batchPnL = transactionService.calculateLiveBatchProfitAndLoss(batchId, organisationId);
        return new ResponseEntity<>(batchPnL, HttpStatus.OK);
    }

    @GetMapping("/farm/{farmId}/organisation/{organisationId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Fetch Farm Facility Transactions",
            description = "Compiles a unified transaction history occurring within a distinct physical farm installation."
    )
    public ResponseEntity<List<TransactionResponseDto>> getFarmTransaction(
            @PathVariable Long farmId,
            @PathVariable Long organisationId
    ) {
        List<TransactionResponseDto> farmTransactions = transactionService.getFarmTransaction(farmId, organisationId);
        return new ResponseEntity<>(farmTransactions, HttpStatus.OK);
    }

    @GetMapping("/pnl/farm/{farmId}/organisation/{organisationId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Calculate Total Farm Facility P&L",
            description = "Aggregates overall profitability ratios across every structural segment and housing division inside a specified farm branch location."
    )
    public ResponseEntity<Double> calculateFarmProfitAndLoss(
            @PathVariable Long farmId,
            @PathVariable Long organisationId
    ) {
        Double farmPnL = transactionService.calculateFarmProfitAndLoss(farmId, organisationId);
        return new ResponseEntity<>(farmPnL, HttpStatus.OK);
    }
}