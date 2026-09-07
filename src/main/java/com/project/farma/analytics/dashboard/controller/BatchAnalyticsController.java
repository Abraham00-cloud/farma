package com.project.farma.analytics.dashboard.controller; // Adjusted to new package name

import com.project.farma.analytics.dashboard.dto.BatchPerformanceDashboardDto;
import com.project.farma.analytics.dashboard.service.BatchAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Flock Analytics & Dashboard", description = "Performance analytics, FCR calculations, and flock health summaries")
public class BatchAnalyticsController {

    private final BatchAnalyticsService batchAnalyticsService;

    @GetMapping("/batch/{batchId}/dashboard")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')") // ADDED SECURITY
    @Operation(summary = "Get comprehensive real-time performance dashboard for a batch")
    public ResponseEntity<BatchPerformanceDashboardDto> getBatchDashboard(@PathVariable Long batchId) {
        return ResponseEntity.ok(batchAnalyticsService.getBatchPerformanceDashboard(batchId));
    }
}