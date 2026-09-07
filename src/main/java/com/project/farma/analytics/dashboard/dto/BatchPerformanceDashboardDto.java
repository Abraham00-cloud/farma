package com.project.farma.analytics.dashboard.dto;

import java.time.LocalDate;

public record BatchPerformanceDashboardDto(
        Long batchId,
        String batchNumber,
        String sectionName,
        String breed,

        Integer initialCount,
        Integer currentCount,
        Integer totalMortality,
        Double mortalityRatePercentage,
        Double survivabilityRatePercentage,

        Double totalFeedConsumedKg,
        Double currentAverageWeightGrams,
        Double calculatedFcr,

        Long activeAlertsCount,
        Long resolvedAlertsCount,

        LocalDate startDate,
        Integer currentAgeInDays
) {
}
