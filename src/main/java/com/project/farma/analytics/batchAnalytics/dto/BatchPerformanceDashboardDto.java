package com.project.farma.analytics.batchAnalytics.dto;

import java.time.LocalDate;

public record BatchPerformanceDashboardDto(
        Long batchId,
        String batchNumber,
        String sectionName,
        String breed,

        // Flock Population Tracking
        Integer initialCount,
        Integer currentCount,
        Integer totalMortality,
        Double mortalityRatePercentage,
        Double survivabilityRatePercentage,

        // Feed & Weight Metrics
        Double totalFeedConsumedKg,
        Double currentAverageWeightGrams,
        Double calculatedFcr, // Feed Conversion Ratio

        // Alert & Health Metrics
        Long activeAlertsCount,
        Long resolvedAlertsCount,

        LocalDate startDate,
        Integer currentAgeInDays
) {
}
