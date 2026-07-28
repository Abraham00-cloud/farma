package com.project.farma.analytics.batchAnalytics.service;

import com.project.farma.dailyLogs.model.DailyLog;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlockPerformanceCalculator {
    public double calculateTotalFeedConsumed(List<DailyLog> logs) {
        return logs.stream()
                .mapToDouble(log -> log.getFeedQuantityUsed() != null ? log.getFeedQuantityUsed() : 0.0)
                .sum();
    }

    public double extractLatestAverageWeight(List<DailyLog> logs) {
        if (logs.isEmpty()) return 0.0;
        return logs.getLast().getAverageWeight();
    }

    public double calculateMortalityRate(int totalMortality, int initialCount) {
        if (initialCount == 0) return 0.0;
        return roundToTwoDecimals(((double) totalMortality / initialCount) * 100.0);
    }

    public double calculateSurvivabilityRate(double mortalityRate) {
        return roundToTwoDecimals(100.0 - mortalityRate);
    }

    public double calculateFCR(double totalFeedKg, int currentBirdCount, double avgWeightGrams) {
        if (currentBirdCount == 0 || avgWeightGrams == 0.0) return 0.0;
        double totalFlockWeightKg = (currentBirdCount * avgWeightGrams) / 1000.0;
        if (totalFlockWeightKg == 0.0) return 0.0;
        return roundToTwoDecimals(totalFeedKg / totalFlockWeightKg);
    }

    public double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
