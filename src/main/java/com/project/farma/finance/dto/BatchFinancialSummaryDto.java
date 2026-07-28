package com.project.farma.finance.dto;

public record BatchFinancialSummaryDto(
        Long batchId,
        String batchNumber,
        String sectionName,
        String breed,
        String status,
        Double revenue,
        Double expenses,
        Double netProfit,
        Double marginPercentage,
        Double costPerBird,
        Double profitPerBird
) {
}
