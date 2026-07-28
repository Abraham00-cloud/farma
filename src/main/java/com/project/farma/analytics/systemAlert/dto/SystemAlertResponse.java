package com.project.farma.analytics.systemAlert.dto;

import com.project.farma.analytics.systemAlert.model.AlertStatus;
import com.project.farma.analytics.systemAlert.model.AlertType;

import java.time.LocalDateTime;

public record SystemAlertResponse(
        Long id,
        Long batchId,
        String batchNumber,
        AlertType alertType,
        AlertStatus status,
        String diagnosisMessage,
        String resolutionNotes,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}
