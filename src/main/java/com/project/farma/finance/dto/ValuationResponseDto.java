package com.project.farma.finance.dto;


public record ValuationResponseDto(
        String scope,
        String scopeName,
        Integer totalLiveBirds,
        Double estimatedTotalWeightKg,
        Double totalProduceInStock,
        Double projectedMeatRevenue,
        Double projectedProduceRevenue,
        Double totalProjectedRevenue,
        Double totalIncurredCosts,
        Double projectedNetProfit,
        Double projectedProfitMarginPercentage
) {}
