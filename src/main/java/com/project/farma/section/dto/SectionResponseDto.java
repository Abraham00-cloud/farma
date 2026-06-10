package com.project.farma.section.dto;

import com.project.farma.section.model.AnimalCategory;
import com.project.farma.section.model.ProductionType;

public record SectionResponseDto(
        Long id,
        String name,
        Long farmId,
        AnimalCategory animalCategory,
        ProductionType productionType,
        Integer capacity
) {
}
