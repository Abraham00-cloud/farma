package com.project.farma.transaction.controller;

import com.project.farma.transaction.dto.TransactionRequestDto;
import com.project.farma.transaction.dto.TransactionResponseDto;
import com.project.farma.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
    @Operation(summary = "Record External Cash Flow Transaction")
    public ResponseEntity<TransactionResponseDto> createTransaction(@Valid @RequestBody TransactionRequestDto requestDto) {
        TransactionResponseDto transaction = transactionService.createTransaction(requestDto);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @GetMapping("/organisation/{organisationId}")
    @PreAuthorize("hasRole('PROPRIETOR')")
    @Operation(summary = "Fetch Complete Organisation Ledger")
    public ResponseEntity<Page<TransactionResponseDto>> getOrganisationLedger(
            @PathVariable Long organisationId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<TransactionResponseDto> ledger = transactionService.getAllTransactionsByOrganisation(organisationId, pageable);
        return new ResponseEntity<>(ledger, HttpStatus.OK);
    }

    @GetMapping("/cash-flow/organisation/{organisationId}")
    @PreAuthorize("hasRole('PROPRIETOR')")
    @Operation(summary = "Fetch Company Cash Flow Statement")
    public ResponseEntity<Page<TransactionResponseDto>> getCompanyCashFlow(
            @PathVariable Long organisationId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<TransactionResponseDto> cashFlow = transactionService.getCompanyCashFlow(organisationId, pageable);
        return new ResponseEntity<>(cashFlow, HttpStatus.OK);
    }

    @GetMapping("/ledger/batch/{batchId}/organisation/{organisationId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch Detailed Batch Ledger")
    public ResponseEntity<Page<TransactionResponseDto>> getBatchLedger(
            @PathVariable Long batchId,
            @PathVariable Long organisationId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<TransactionResponseDto> ledger = transactionService.getBatchTransactions(batchId, organisationId, pageable);
        return new ResponseEntity<>(ledger, HttpStatus.OK);
    }

    @GetMapping("/farm/{farmId}/organisation/{organisationId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch Farm Facility Transactions")
    public ResponseEntity<Page<TransactionResponseDto>> getFarmTransaction(
            @PathVariable Long farmId,
            @PathVariable Long organisationId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        Page<TransactionResponseDto> farmTransactions = transactionService.getFarmTransaction(farmId, organisationId, pageable);
        return new ResponseEntity<>(farmTransactions, HttpStatus.OK);
    }

    @GetMapping("/export/organisation/{organisationId}")
    @PreAuthorize("hasRole('PROPRIETOR')")
    @Operation(summary = "Export ledger to CSV for auditing based on a date window")
    public ResponseEntity<byte[]> exportLedgerToCsv(
            @PathVariable Long organisationId,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {

        byte[] csvData = transactionService.exportTransactionsToCsv(organisationId, startDate, endDate);
        String filename = String.format("Farma_Audit_%s_to_%s.csv", startDate, endDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csvData);
    }

    @GetMapping("/pnl/batch/{batchId}/organisation/{organisationId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Calculate Live Batch Profit & Loss")
    public ResponseEntity<Double> calculateLiveBatchProfitAndLoss(
            @PathVariable Long batchId, @PathVariable Long organisationId) {
        Double batchPnL = transactionService.calculateLiveBatchProfitAndLoss(batchId, organisationId);
        return new ResponseEntity<>(batchPnL, HttpStatus.OK);
    }

    @GetMapping("/pnl/farm/{farmId}/organisation/{organisationId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Calculate Total Farm Facility P&L")
    public ResponseEntity<Double> calculateFarmProfitAndLoss(
            @PathVariable Long farmId, @PathVariable Long organisationId) {
        Double farmPnL = transactionService.calculateFarmProfitAndLoss(farmId, organisationId);
        return new ResponseEntity<>(farmPnL, HttpStatus.OK);
    }
}