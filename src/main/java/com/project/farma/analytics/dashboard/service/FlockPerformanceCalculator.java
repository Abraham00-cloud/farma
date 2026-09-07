package com.project.farma.analytics.dashboard.service;

import com.project.farma.dailyLogs.model.DailyLog;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlockPerformanceCalculator {

    private static final double STANDARD_BAG_WEIGHT_KG = 25.0;

    public double calculateTotalFeedConsumedInUnits(List<DailyLog> logs) {
        return logs.stream()
                .mapToDouble(log -> log.getFeedQuantityUsed() != null ? log.getFeedQuantityUsed() : 0.0)
                .sum();
    }

    public double extractLatestAverageWeight(List<DailyLog> logs) {
        if (logs.isEmpty()) return 0.0;

        for (int i = logs.size() - 1; i >= 0; i--) {
            Double weight = logs.get(i).getAverageWeight();
            if (weight != null && weight > 0.0) {
                return weight;
            }
        }
        return 0.0;
    }

    public double calculateMortalityRate(int totalMortality, int initialCount) {
        if (initialCount == 0) return 0.0;
        return roundToTwoDecimals(((double) totalMortality / initialCount) * 100.0);
    }

    public double calculateSurvivabilityRate(double mortalityRate) {
        return roundToTwoDecimals(100.0 - mortalityRate);
    }

    public double calculateFCR(double totalFeedUnits, int currentBirdCount, double avgWeightKg) {
        if (currentBirdCount == 0 || avgWeightKg == 0.0) return 0.0;

        double totalFeedKg = totalFeedUnits * STANDARD_BAG_WEIGHT_KG;

        double totalFlockWeightKg = currentBirdCount * avgWeightKg;

        if (totalFlockWeightKg == 0.0) return 0.0;

        return roundToTwoDecimals(totalFeedKg / totalFlockWeightKg);
    }

    public double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}