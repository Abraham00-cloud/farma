package com.project.farma.farm.dto;

import java.time.LocalDateTime;

public record FarmResponseDto (
        Long id,
        String name,
        String address,
        Long organisationId,
        String mangerName,
        LocalDateTime createdAt
) {
}
