package com.project.farma.batch.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BatchRequestDto(
        @NotBlank(message = "Batch number is required")
        String batchNumber,
        @NotNull(message = "Section id is required")
        Long sectionId,
        @NotNull(message = "Initial count is required")
        @Min(value = 1, message = "Initial count must be at least 1")
        Integer initialCount,
        @NotNull(message = "Start date is required")
        LocalDate startDate,
        @NotNull(message = "Expected end date is required")
        LocalDate expectedEndDate


) {
}
