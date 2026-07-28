package com.project.farma.finance.dto;

import java.util.List;

public record BatchFinancialPnlResponseDto(
        Long batchId,
        String batchNumber,

        // Core P&L Summary Cards
        Double totalRevenue,
        Double totalExpenses,
        Double netProfitOrLoss,
        Double profitMarginPercentage,

        // Unit Economics
        Double costPerBird,
        Double revenuePerBird,
        Double profitPerBird,

        // Chart Data Breakdown
        List<FinancialCategoryBreakdownDto> expenseBreakdownChart,

        Boolean isProfitable
) {
}
