package com.project.farma.finance.controller;

import com.project.farma.finance.dto.BatchFinancialPnlResponseDto;
import com.project.farma.finance.dto.FarmFinancialOverviewDto;
import com.project.farma.finance.service.FinancialPnlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/financials")
@RequiredArgsConstructor
@Tag(name = "Financial Analytics & P&L Engine", description = "Endpoints for batch P&L statements, unit economics, and expense breakdowns")
public class FinancialPnlController {

    private final FinancialPnlService financialPnlService;

    @GetMapping("/batch/{batchId}/pnl")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Get complete financial P&L statement and chart breakdown for a batch")
    public ResponseEntity<BatchFinancialPnlResponseDto> getBatchPnl(@PathVariable Long batchId) {
        return ResponseEntity.ok(financialPnlService.getBatchPnlAnalysis(batchId));
    }

    @GetMapping("/farm/{farmId}/overview")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Get farm-level financial rollup, batch comparison list, and category expense chart")
    public ResponseEntity<FarmFinancialOverviewDto> getFarmFinancialOverview(@PathVariable Long farmId) {
        return ResponseEntity.ok(financialPnlService.getFarmFinancialOverview(farmId));
    }
}