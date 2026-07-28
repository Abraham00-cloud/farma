package com.project.farma.batch.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public record BatchCloseRequestDto(
        @NotNull(message = "Actual harvest completion date is required")
        LocalDate actualEndDate,

        @NotNull(message = "Final sold bird count is required")
        @PositiveOrZero(message = "Sold bird count cannot be negative")
        Integer totalBirdsSold,

        @NotNull(message = "Total revenue generated from harvest sale is required")
        @Positive(message = "Total sale revenue must be greater than zero")
        Double totalSaleRevenue,

        String harvestNotes
) {
}
