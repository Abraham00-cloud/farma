package com.project.farma.dailyLogs.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DailyLogRequestDto (
        @NotNull(message = "Batch ID is required")
        Long batchId,
        @NotNull(message = "Log date is required")
        @PastOrPresent(message = "Log date cannot be in the future")
        LocalDate logDate,

        Long feedInventoryId,
        @Min(value = 0, message = "Feed quantity used cannot be negative")
        Double feedQuantityUsed,

        Long medicineInventoryId,
        @Min(value = 0, message = "Medicine quantity used cannot be negative")
        Double medicineQuantityUsed,
        @Size(max = 100, message = "Administration method description is too long")
        String administrationMethod,

        @NotNull(message = "Mortality count is required") // Default to 0 in UI, but must be present
        @Min(value = 0, message = "Mortality count cannot be negative")
        Integer mortalityCount,

        @Min(value = 0, message = "Average weight cannot be negative")
        Double averageWeight,

        @Size(max = 500, message = "Observations must not exceed 500 characters")
        String observations,

        Long assignedToId
) {

}
