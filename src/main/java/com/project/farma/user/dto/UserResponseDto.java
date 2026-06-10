package com.project.farma.user.dto;

import com.project.farma.user.model.Role;

import java.time.LocalDateTime;

public record UserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role,
        Long organisationId,
        Long parentId,
        LocalDateTime createdAt
) {
}
