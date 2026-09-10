package com.project.farma.finance.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ValuationRequestDto(
        @NotNull(message = "Scope is required (BATCH, FARM, ORGANISATION)")
        String scope,

        @NotNull(message = "Scope ID is required")
        Long scopeId,

        @Min(value = 0, message = "Price per Kg cannot be negative")
        Double projectedPricePerKg, // For Broilers/Meat

        @Min(value = 0, message = "Price per piece of produce cannot be negative")
        Double projectedPricePerProduceUnit // For Layers/Eggs
) {}
