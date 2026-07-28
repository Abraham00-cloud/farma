package com.project.farma.analytics.systemAlert.dto;

import com.project.farma.analytics.systemAlert.model.AlertResolutionCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlertResolutionRequest(
        @NotNull(message = "Specific resolution action category is required")
        AlertResolutionCategory actionCategory,

        @NotNull(message = "Primary corrective measure taken must be specified")
        @Size(min = 5, message = "Corrective measure description must be at least 5 characters")
        String actionTaken,

        Double verifiedTemperature,
        Double verifiedWaterPressure,

        String supervisorNotes
) {
}
