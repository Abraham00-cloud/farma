package com.project.farma.farm.dto;

import java.time.LocalDateTime;

public record FarmResponseDto (
        Long id,
        String name,
        Long organisationId,
        Long managerId,
        String address,
        Double latitude,
        Double longitude,
        boolean isActive,
        LocalDateTime createdAt
) {
}
