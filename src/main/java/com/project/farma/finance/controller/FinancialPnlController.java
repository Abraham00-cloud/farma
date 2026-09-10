package com.project.farma.finance.controller;

import com.project.farma.finance.dto.BatchFinancialPnlResponseDto;
import com.project.farma.finance.dto.FarmFinancialOverviewDto;
import com.project.farma.finance.dto.ValuationRequestDto;
import com.project.farma.finance.dto.ValuationResponseDto;
import com.project.farma.finance.service.FinancialPnlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/estimator/profit")
    @PreAuthorize("hasRole('PROPRIETOR')")
    @Operation(
            summary = "Calculate Projected Profit (What-If Scenario)",
            description = "Aggregates live bird counts, weights, and produce stock, applies hypothetical market pricing, deducts actual sunk costs, and returns a real-time projected profit margin."
    )
    public ResponseEntity<ValuationResponseDto> calculateProjectedValuation(
            @Valid @RequestBody ValuationRequestDto requestDto) {
        ValuationResponseDto projection = financialPnlService.calculateProjectedValuation(requestDto);
        return ResponseEntity.ok(projection);
    }
}