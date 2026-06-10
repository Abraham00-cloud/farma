package com.project.farma.user.dto;

import com.project.farma.user.model.Role;

public record AuthResponseDto (
        String token,
        String email,
        Role role,
        Long organisationId
) {
}
