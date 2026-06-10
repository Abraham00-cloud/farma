package com.project.farma.organisation.dto;

import com.project.farma.organisation.model.OrganisationType;

import java.time.LocalDateTime;

public record OrganisationResponseDto (
        Long id,
        String name,
        String registrationNumber,
        OrganisationType organisationType,
        LocalDateTime createdAt
) {

}
