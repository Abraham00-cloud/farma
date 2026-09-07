package com.project.farma.analytics.dashboard.service;

import com.project.farma.analytics.dashboard.dto.BatchPerformanceDashboardDto;
import com.project.farma.analytics.dashboard.mapper.BatchAnalyticsMapper;
import com.project.farma.analytics.alert.service.SystemAlertService;
import com.project.farma.batch.model.Batch;
import com.project.farma.batch.service.BatchService;
import com.project.farma.dailyLogs.model.DailyLog;
import com.project.farma.dailyLogs.service.DailyLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchAnalyticsService {

    private final BatchService batchService;
    private final DailyLogService dailyLogService;
    private final SystemAlertService systemAlertService;
    private final FlockPerformanceCalculator calculator;
    private final BatchAnalyticsMapper batchAnalyticsMapper;

    private static final double STANDARD_BAG_WEIGHT_KG = 25.0;

    public BatchPerformanceDashboardDto getBatchPerformanceDashboard(Long batchId) {
        Batch batch = batchService.getBatchById(batchId);
        List<DailyLog> logs = dailyLogService.getLogEntitiesForBatch(batchId);

        long activeAlerts = systemAlertService.countActiveAlerts(batchId);
        long resolvedAlerts = systemAlertService.countResolvedAlerts(batchId);

        double totalFeedUnits = calculator.calculateTotalFeedConsumedInUnits(logs);
        double latestWeightKg = calculator.extractLatestAverageWeight(logs);

        double mortalityRate = calculator.calculateMortalityRate(batch.getMortalityCount(), batch.getInitialCount());
        double survivability = calculator.calculateSurvivabilityRate(mortalityRate);

        double fcr = calculator.calculateFCR(totalFeedUnits, batch.getCurrentCount(), latestWeightKg);

        double totalFeedKgForDisplay = totalFeedUnits * STANDARD_BAG_WEIGHT_KG;
        double latestWeightGramsForDisplay = latestWeightKg * 1000.0;

        return batchAnalyticsMapper.toPerformanceDashboard(
                batch,
                totalFeedKgForDisplay,
                latestWeightGramsForDisplay,
                mortalityRate,
                survivability,
                fcr,
                activeAlerts,
                resolvedAlerts
        );
    }
}