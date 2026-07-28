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
        Long organisationId,
        @NotNull(message = "Latitude coordinate is required for weather engine geofencing")
        Double latitude,
        @NotNull(message = "Longitude coordinate is required for weather engine geofencing")
        Double longitude,
        boolean isActive


) {
}
