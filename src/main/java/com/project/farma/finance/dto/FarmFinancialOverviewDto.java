package com.project.farma.finance.dto;

import java.util.List;

public record FarmFinancialOverviewDto(
        Long farmId,
        String farmName,
        Double totalRevenue,
        Double totalExpenses,
        Double totalNetProfit,
        Double overallMarginPercentage,
        Integer totalBatchesCount,
        Integer activeBatchesCount,
        Integer completedBatchesCount,
        List<BatchFinancialSummaryDto> batchSummaries,
        List<FinancialCategoryBreakdownDto> expenseBreakdownChart
) {
}
