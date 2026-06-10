package com.project.farma.dailyLogs.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DailyLogResponseDto(
        Long id,
        LocalDate logDate,

        Long batchId,
        String batchName,

        String feedName,
        Double feedQuantityUsed,

        String medicineName,
        Double medicineQuantityUsed,

        Integer mortalityCount,
        Double averageWeight,

        String observations,

        String recordedByName,
        String assignedToName,

        LocalDateTime createdAt
) {
}
