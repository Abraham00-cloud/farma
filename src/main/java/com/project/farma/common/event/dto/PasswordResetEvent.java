package com.project.farma.common.event.dto;

public record PasswordResetEvent(
        String email,
        String resetUrl
) {
}
