package com.project.farma.analytics.batchAnalytics.service;


import com.project.farma.analytics.batchAnalytics.dto.BatchPerformanceDashboardDto;
import com.project.farma.analytics.batchAnalytics.mapper.BatchAnalyticsMapper;
import com.project.farma.analytics.systemAlert.service.SystemAlertService;
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

    public BatchPerformanceDashboardDto getBatchPerformanceDashboard(Long batchId) {
        Batch batch = batchService.getBatchById(batchId);
        List<DailyLog> logs = dailyLogService.getLogsForBatch(batchId);
        long activeAlerts = systemAlertService.countActiveAlerts(batchId);
        long resolvedAlerts = systemAlertService.countResolvedAlerts(batchId);


        double totalFeed = calculator.calculateTotalFeedConsumed(logs);
        double latestWeight = calculator.extractLatestAverageWeight(logs);
        double mortalityRate = calculator.calculateMortalityRate(batch.getMortalityCount(), batch.getInitialCount());
        double survivability = calculator.calculateSurvivabilityRate(mortalityRate);
        double fcr = calculator.calculateFCR(totalFeed, batch.getCurrentCount(), latestWeight);


        return batchAnalyticsMapper.toPerformanceDashboard(
                batch, totalFeed, latestWeight, mortalityRate, survivability, fcr, activeAlerts, resolvedAlerts
        );
    }
}
