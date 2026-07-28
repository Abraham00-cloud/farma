package com.project.farma.analytics.systemAlert.mapper;

import com.project.farma.analytics.systemAlert.dto.SystemAlertResponse;
import com.project.farma.analytics.systemAlert.model.SystemAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemAlertMapper {
    public SystemAlertResponse toResponse(SystemAlert alert) {
        if (alert == null) {
            return null;
        }
        return new SystemAlertResponse(
                alert.getId(),
                alert.getBatch().getId(),
                alert.getBatch().getBatchNumber(),
                alert.getAlertType(),
                alert.getStatus(),
                alert.getDiagnosisMessage(),
                null,
                alert.getCreatedAt(),
                alert.getResolvedAt()
        );
    }
}
