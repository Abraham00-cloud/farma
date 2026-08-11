package com.project.farma.common.event.dto;

import com.project.farma.organisation.model.OrganisationType;

public record OrganisationRegisteredEvent(
        String email,
        String proprietorName,
        String orgName,
        String registrationNumber,
        OrganisationType organisationType
) {
}
