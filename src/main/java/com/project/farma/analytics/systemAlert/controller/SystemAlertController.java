package com.project.farma.analytics.systemAlert.controller;

import com.project.farma.analytics.systemAlert.dto.AlertResolutionRequest;
import com.project.farma.analytics.systemAlert.dto.SystemAlertResponse;
import com.project.farma.analytics.systemAlert.service.SystemAlertService;
import com.project.farma.security.FarmUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Biosecurity Alerts & System State Machine", description = "Endpoints for monitoring and resolving farm alerts")
public class SystemAlertController {

    private final SystemAlertService systemAlertService;

    @GetMapping("/batch/{batchId}")
    @Operation(summary = "Get active alerts for a batch")
    public ResponseEntity<List<SystemAlertResponse>> getActiveAlertsForBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(systemAlertService.getActiveAlertsForBatch(batchId));
    }

    @PatchMapping("/{alertId}/acknowledge")
    @Operation(summary = "Acknowledge an active alert (In Progress)")
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable Long alertId) {
        systemAlertService.acknowledgeAlert(alertId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{alertId}/resolve")
    @Operation(summary = "Resolve a triggered biosecurity alert with auditable corrective action")
    public ResponseEntity<SystemAlertResponse> resolveAlert(
            @PathVariable Long alertId,
            @Valid @RequestBody AlertResolutionRequest requestDto,
            @AuthenticationPrincipal FarmUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(systemAlertService.resolveAlert(alertId, requestDto, currentUser.getId()));
    }
}