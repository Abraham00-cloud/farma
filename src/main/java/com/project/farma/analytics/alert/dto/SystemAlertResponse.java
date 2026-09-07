package com.project.farma.analytics.alert.dto;

import com.project.farma.analytics.alert.model.AlertStatus;
import com.project.farma.analytics.alert.model.AlertType;

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
