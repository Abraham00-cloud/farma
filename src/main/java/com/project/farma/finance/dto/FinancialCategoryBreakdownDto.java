package com.project.farma.finance.dto;

public record FinancialCategoryBreakdownDto(
        String category,
        Double totalAmount,
        Double percentageOfTotalCost
) {
}
