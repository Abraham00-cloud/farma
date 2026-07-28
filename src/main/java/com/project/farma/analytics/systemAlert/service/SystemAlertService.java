package com.project.farma.analytics.systemAlert.service;

import com.project.farma.analytics.systemAlert.dto.AlertResolutionRequest;
import com.project.farma.analytics.systemAlert.dto.SystemAlertResponse;
import com.project.farma.analytics.systemAlert.mapper.SystemAlertMapper;
import com.project.farma.analytics.systemAlert.model.AlertStatus;
import com.project.farma.analytics.systemAlert.model.AlertType;
import com.project.farma.analytics.systemAlert.model.SystemAlert;
import com.project.farma.analytics.systemAlert.repository.SystemAlertRepository;
import com.project.farma.batch.model.Batch;
import com.project.farma.dailyLogs.model.DailyLog;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemAlertService {

    private final SystemAlertRepository systemAlertRepository;
    private final SystemAlertMapper systemAlertMapper;


    @Transactional
    public void acknowledgeAlert(Long alertId) {
        updateAlertStatus(alertId, AlertStatus.ACKNOWLEDGED, null);
    }

    @Transactional
    public SystemAlertResponse resolveAlert(Long alertId, AlertResolutionRequest requestDto, Long currentUserId) {
        SystemAlert alert = fetchAlertById(alertId);

        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Alert is already resolved.");
        }

        applyResolutionData(alert, requestDto, currentUserId);
        SystemAlert savedAlert = systemAlertRepository.save(alert);

        log.info("System Alert [{}] on Batch [{}] RESOLVED via [{}] by User [{}]",
                alertId, alert.getBatch().getBatchNumber(), requestDto.actionCategory(), currentUserId);

        return systemAlertMapper.toResponse(savedAlert);
    }

    @Transactional
    public void triggerInternalAlert(Batch batch, DailyLog dailyLog, AlertType type, String message) {
        if (hasActiveAlert(batch.getId(), type)) {
            log.debug("Alert suppression active: Unresolved {} alert exists for Batch {}", type, batch.getBatchNumber());
            return;
        }

        SystemAlert newAlert = SystemAlert.builder()
                .batch(batch)
                .dailyLog(dailyLog)
                .alertType(type)
                .status(AlertStatus.TRIGGERED)
                .diagnosisMessage(message)
                .build();

        systemAlertRepository.save(newAlert);
        log.warn("💾 System State Machine Alert Created: [{}] on Batch {}", type, batch.getBatchNumber());
    }

    public List<SystemAlertResponse> getActiveAlertsForBatch(Long batchId) {
        return systemAlertRepository.findByBatchIdAndStatusIn(
                batchId,
                List.of(AlertStatus.TRIGGERED, AlertStatus.ACKNOWLEDGED)
        ).stream().map(systemAlertMapper::toResponse).toList();
    }

    public long countActiveAlerts(Long batchId) {
        return systemAlertRepository.countByBatchIdAndStatusIn(
                batchId, List.of(AlertStatus.TRIGGERED, AlertStatus.ACKNOWLEDGED)
        );
    }

    public long countResolvedAlerts(Long batchId) {
        return systemAlertRepository.countByBatchIdAndStatus(batchId, AlertStatus.RESOLVED);
    }



    private SystemAlert fetchAlertById(Long alertId) {
        return systemAlertRepository.findById(alertId)
                .orElseThrow(() -> new EntityNotFoundException("Alert record not found with ID: " + alertId));
    }

    private boolean hasActiveAlert(Long batchId, AlertType type) {
        return systemAlertRepository.existsByBatchIdAndAlertTypeAndStatusIn(
                batchId, type, List.of(AlertStatus.TRIGGERED, AlertStatus.ACKNOWLEDGED)
        );
    }

    private void updateAlertStatus(Long alertId, AlertStatus status, LocalDateTime timestamp) {
        SystemAlert alert = fetchAlertById(alertId);
        alert.setStatus(status);
        if (timestamp != null) {
            alert.setResolvedAt(timestamp);
        }
        systemAlertRepository.save(alert);
    }

    private void applyResolutionData(SystemAlert alert, AlertResolutionRequest dto, Long userId) {
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolutionCategory(dto.actionCategory());
        alert.setActionTaken(dto.actionTaken());
        alert.setResolutionNotes(dto.supervisorNotes());
        alert.setResolvedByUserId(userId);
    }
}