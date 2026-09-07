package com.project.farma.batch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PartialSaleRequestDto(
        @NotNull(message = "Sale date is required")
        LocalDate saleDate,

        @NotNull(message = "Number of birds sold is required")
        @Min(value = 1, message = "Must sell at least 1 bird")
        Integer birdsSold,

        @NotNull(message = "Sale revenue is required")
        @Min(value = 0, message = "Revenue cannot be negative")
        Double saleRevenue,

        String notes
) {
}