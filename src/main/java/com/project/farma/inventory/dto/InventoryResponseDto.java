package com.project.farma.inventory.dto;

import com.project.farma.inventory.model.InventoryCategory;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InventoryResponseDto(
        Long id,
        String name,
        String category,
        Long farmId,
        String farmName,
        Double currentQuantity,
        String unitPrice,

        Double totalValue,

        LocalDate expiryDate,
        Double lowStockThreshold,

        boolean isLowStock


) {
}
