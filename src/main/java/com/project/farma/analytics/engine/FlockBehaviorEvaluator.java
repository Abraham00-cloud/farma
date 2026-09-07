package com.project.farma.analytics.engine;

import com.project.farma.analytics.alert.model.AlertType;
import com.project.farma.analytics.alert.service.SystemAlertService;
import com.project.farma.analytics.benchmark.model.WeeklyBreedStandard;
import com.project.farma.batch.model.Batch;
import com.project.farma.dailyLogs.model.DailyLog;
import com.project.farma.dailyLogs.service.DailyLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FlockBehaviorEvaluator implements DailyLogEvaluator {

    private final DailyLogService dailyLogService;
    private final SystemAlertService alertService;

    public FlockBehaviorEvaluator(@Lazy DailyLogService dailyLogService, SystemAlertService alertService) {
        this.dailyLogService = dailyLogService;
        this.alertService = alertService;
    }

    private double getSafeFeed(DailyLog log) {
        return log.getFeedQuantityUsed() != null ? log.getFeedQuantityUsed() : 0.0;
    }

    @Override
    public void evaluate(DailyLog currentLog, Batch activeBatch, WeeklyBreedStandard standard, Map<String, Double> environment) {
        LocalDate today = currentLog.getLogDate();
        LocalDate threeDaysAgo = today.minusDays(2);

        List<DailyLog> logs = dailyLogService.getLogEntitiesForBatchInWindow(activeBatch.getId(), threeDaysAgo, today);

        if (logs.size() < 3) return;

        boolean isSequential = logs.get(0).getLogDate().plusDays(1).equals(logs.get(1).getLogDate()) &&
                logs.get(1).getLogDate().plusDays(1).equals(logs.get(2).getLogDate());

        if (!isSequential) {
            log.debug("Logs are not strictly sequential. Skipping variance evaluation.");
            return;
        }

        DailyLog day1 = logs.get(0);
        DailyLog day2 = logs.get(1);
        DailyLog day3 = logs.get(2);

        int headcount = activeBatch.getCurrentCount();
        if (headcount <= 0) return;

        double totalFeed3Days = getSafeFeed(day1) + getSafeFeed(day2) + getSafeFeed(day3);
        double totalWater3Days = (day1.getWaterQuantityUsed() != null ? day1.getWaterQuantityUsed() : 0) +
                (day2.getWaterQuantityUsed() != null ? day2.getWaterQuantityUsed() : 0) +
                (day3.getWaterQuantityUsed() != null ? day3.getWaterQuantityUsed() : 0);

        if (totalFeed3Days > 0 && totalWater3Days > 0) {
            double rollingWfr = totalWater3Days / totalFeed3Days;
            if (rollingWfr < 1.5) {
                String message = String.format("🚨 HEALTH RISK: Acute Water Dehydration. The 72-hour Water-to-Feed Ratio has dropped to %.2f...", rollingWfr);
                alertService.triggerInternalAlert(activeBatch, currentLog, AlertType.CLIMATE_STRESS, message);
            }
        }

        if (totalFeed3Days > 0) {
            double avgFeed = totalFeed3Days / 3.0;
            double feedVariance = (Math.pow(getSafeFeed(day1) - avgFeed, 2) +
                    Math.pow(getSafeFeed(day2) - avgFeed, 2) +
                    Math.pow(getSafeFeed(day3) - avgFeed, 2)) / 3.0;
            double feedVolatility = (Math.sqrt(feedVariance) / avgFeed) * 100.0;

            if (feedVolatility > 18.0) {
                String message = String.format(
                        "⚠️ MANAGEMENT INCONSISTENCY: High operational variance (%.1f%% volatility) detected across the last 3 days. " +
                                "Day 1: %.1f, Day 2: %.1f, Day 3: %.1f.",
                        feedVolatility, getSafeFeed(day1), getSafeFeed(day2), getSafeFeed(day3)
                );
                alertService.triggerInternalAlert(activeBatch, currentLog, AlertType.FEED_UNDER_CONSUMPTION, message);
            }
        }

        if (day3.getAverageWeight() != null && day3.getAverageWeight() > 0 && standard.getExpectedBodyWeight() != null) {
            double currentWeightGrams = day3.getAverageWeight() * 1000.0;
            double targetWeightGrams = standard.getExpectedBodyWeight();

            if (currentWeightGrams < (targetWeightGrams * 0.85)) {
                String message = String.format(
                        "💰 ECONOMIC DRIFT ALERT: Low Feed Conversion Efficiency. Active weight is %.0fg, which lags 15%% behind the breed target (%.0fg). " +
                                "The birds are consuming feed expense but failing to convert it to meat/egg biomass.",
                        currentWeightGrams, targetWeightGrams
                );
                alertService.triggerInternalAlert(activeBatch, currentLog, AlertType.PRODUCTION_YIELD_DROP, message);
            }
        }

        int m1 = day1.getMortalityCount() != null ? day1.getMortalityCount() : 0;
        int m2 = day2.getMortalityCount() != null ? day2.getMortalityCount() : 0;
        int m3 = day3.getMortalityCount() != null ? day3.getMortalityCount() : 0;

        if (m3 > m2 && m2 > m1 && m1 >= 0 && m3 > 0) {
            int totalDeathsWindow = m1 + m2 + m3;
            String message = String.format(
                    "🚨 EXPONENTIAL BIORISK: Accelerated Mortality Velocity detected. Daily deaths are compounding consecutively: Day 1: %d, Day 2: %d, Day 3: %d. Total losses: %d.",
                    m1, m2, m3, totalDeathsWindow
            );
            alertService.triggerInternalAlert(activeBatch, currentLog, AlertType.MORTALITY_LIMIT_BREACH, message);
        }
    }
}