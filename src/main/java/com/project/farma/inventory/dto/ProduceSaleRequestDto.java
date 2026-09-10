package com.project.farma.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProduceSaleRequestDto(
        @NotNull(message = "Inventory ID is required")
        Long inventoryId,

        @NotNull(message = "Quantity sold is required")
        @Min(value = 1, message = "Must sell at least 1 unit")
        Double quantitySold,

        @NotNull(message = "Unit price is required")
        @Min(value = 0, message = "Price cannot be negative")
        Double unitPrice,

        @Size(max = 200, message = "Notes too long")
        String notes
) {}