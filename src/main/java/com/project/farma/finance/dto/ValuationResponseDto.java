package com.project.farma.finance.dto;

public record ValuationResponseDto(
        String scope,
        String scopeName,
        Integer liveBirds,
        Double totalWeightKg,
        Double produceUnits,

        Double realizedRevenue,
        Double totalUnsoldAssetValue,
        Double totalProjectedRevenue,
        Double actualSunkCosts,

        Double projectedNetProfit,
        Double profitMargin
) {}