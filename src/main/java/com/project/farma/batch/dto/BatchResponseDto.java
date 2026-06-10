package com.project.farma.batch.dto;

import com.project.farma.batch.model.Status;
import com.project.farma.section.model.AnimalCategory;
import com.project.farma.section.model.ProductionType;
import com.project.farma.section.model.Section;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BatchResponseDto(
        Long id,
        String batchNumber,
        String sectionName,

        Integer initialCount,
        Integer currentCount,
        Integer mortalityCount,

        AnimalCategory animalCategory,
        ProductionType productionType,

        Status status,
        LocalDate startDate,
        LocalDate expectedEndDate,
        LocalDate actualEndDate,

        LocalDateTime createdAt

) {
}
