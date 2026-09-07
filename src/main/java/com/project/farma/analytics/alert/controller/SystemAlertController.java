package com.project.farma.analytics.alert.controller;

import com.project.farma.analytics.alert.dto.AlertResolutionRequest;
import com.project.farma.analytics.alert.dto.SystemAlertResponse;
import com.project.farma.analytics.alert.service.SystemAlertService;
import com.project.farma.security.FarmUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Get active alerts for a batch")
    public ResponseEntity<List<SystemAlertResponse>> getActiveAlertsForBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(systemAlertService.getActiveAlertsForBatch(batchId));
    }

    @PatchMapping("/{alertId}/acknowledge")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Acknowledge an active alert (In Progress)")
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable Long alertId) {
        systemAlertService.acknowledgeAlert(alertId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{alertId}/resolve")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Resolve a triggered biosecurity alert with auditable corrective action")
    public ResponseEntity<SystemAlertResponse> resolveAlert(
            @PathVariable Long alertId,
            @Valid @RequestBody AlertResolutionRequest requestDto,
            @AuthenticationPrincipal FarmUserPrincipal currentUser
    ) {
        return ResponseEntity.ok(systemAlertService.resolveAlert(alertId, requestDto, currentUser.getId()));
    }
}