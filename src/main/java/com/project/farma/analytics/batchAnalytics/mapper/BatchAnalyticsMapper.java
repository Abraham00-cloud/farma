package com.project.farma.analytics.batchAnalytics.mapper;

import com.project.farma.analytics.batchAnalytics.dto.BatchPerformanceDashboardDto;
import com.project.farma.batch.model.Batch;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class BatchAnalyticsMapper {

    public BatchPerformanceDashboardDto toPerformanceDashboard(
            Batch batch,
            double totalFeedConsumed,
            double latestAvgWeight,
            double mortalityRate,
            double survivabilityRate,
            double fcr,
            long activeAlerts,
            long resolvedAlerts
    ) {
        int ageInDays = (int) ChronoUnit.DAYS.between(batch.getStartDate(), LocalDate.now());

        return new BatchPerformanceDashboardDto(
                batch.getId(),
                batch.getBatchNumber(),
                batch.getSection().getName(),
                batch.getBreed() != null ? batch.getBreed().name() : "N/A",
                batch.getInitialCount(),
                batch.getCurrentCount(),
                batch.getMortalityCount(),
                mortalityRate,
                survivabilityRate,
                totalFeedConsumed,
                latestAvgWeight,
                fcr,
                activeAlerts,
                resolvedAlerts,
                batch.getStartDate(),
                Math.max(ageInDays, 1)
        );
    }
}
