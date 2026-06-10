package com.project.farma.inventory.dto;


import com.project.farma.inventory.model.InventoryCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record InventoryRequestDto(
        @NotBlank(message = "Item name is required")
        String name,
        @NotNull(message = "Item Category is required")
        InventoryCategory category,
        @NotNull(message = "Initial quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        Double quantity,
        @NotBlank(message = "Unit is required")
        String unit,

        @NotNull(message = "FarmId is required")
        Long farmId,

        @NotNull(message = "Unit price is required")
        @Min(value = 0, message = "Price cannot be negative")
        Double unitPrice,

        @NotNull(message = "Low stock threshold is required")
        @Min(value = 0, message = "it cannot be less than zero")
        Double lowStockThreshold,

        @NotNull(message = "Expiry date is required")
        LocalDate expiryDate

) {
}
