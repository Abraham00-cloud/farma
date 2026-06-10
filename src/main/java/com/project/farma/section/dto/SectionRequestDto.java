package com.project.farma.section.dto;

import com.project.farma.section.model.AnimalCategory;
import com.project.farma.section.model.ProductionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SectionRequestDto(
        @NotBlank(message = "Section Name is required")
        String name,

        @NotNull(message = "FarmId is required" )
        Long farmId,

        @NotBlank(message = "Animal Category is required")
        AnimalCategory animalCategory,

        @NotBlank(message = "Production Type is required")
        ProductionType productionType,

        @NotNull(message = "Section Capacity is required")
        Integer capacity


) {
}
