package com.project.farma.common.event.dto;

public record ManagerCreatedEvent(
        String managerName,
        String email,
        String temporaryPassword,
        String orgName
) {
}
