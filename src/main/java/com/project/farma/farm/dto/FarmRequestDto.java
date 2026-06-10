package com.project.farma.farm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FarmRequestDto (
        @NotBlank(message = "Farm name is required")
        String name,
        @NotBlank(message = "Farm address is required")
        String address,
        @NotNull(message = "Farm Manager id is required")
        Long managerId,
        @NotNull(message = "Organisation id is required")
        Long organisationId


) {
}
