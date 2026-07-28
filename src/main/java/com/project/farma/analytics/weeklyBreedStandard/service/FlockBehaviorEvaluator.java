package com.project.farma.analytics.weeklyBreedStandard.service;

import com.project.farma.analytics.systemAlert.model.AlertType;
import com.project.farma.analytics.systemAlert.service.SystemAlertService;
import com.project.farma.analytics.weeklyBreedStandard.model.WeeklyBreedStandard;
import com.project.farma.batch.model.Batch;
import com.project.farma.dailyLogs.model.DailyLog;
import com.project.farma.dailyLogs.service.DailyLogService;
import lombok.RequiredArgsConstructor;
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

    public FlockBehaviorEvaluator(  @Lazy DailyLogService dailyLogService, SystemAlertService alertService) {
        this.dailyLogService = dailyLogService;
        this.alertService = alertService;
    }

    @Override
    public void evaluate(DailyLog currentLog, Batch activeBatch, WeeklyBreedStandard standard, Map<String, Double> environment) {
        LocalDate today = currentLog.getLogDate();
        LocalDate threeDaysAgo = today.minusDays(2);

        // Fetch consecutive history using our clean domain service boundary
        List<DailyLog> logs = dailyLogService.getLogsForBatchInWindow(activeBatch.getId(), threeDaysAgo, today);

        if (logs.size() < 3) {
            log.debug("Operational diagnostic window deferred for Batch {}: Waiting for 3 days of consecutive records.", activeBatch.getBatchNumber());
            return;
        }

        // Extract metrics for 3 consecutive days
        DailyLog day1 = logs.get(0);
        DailyLog day2 = logs.get(1);
        DailyLog day3 = logs.get(2); // today

        int headcount = activeBatch.getCurrentCount();
        if (headcount <= 0) return;

        // =========================================================================
        // 1. ANALYSIS PATH A: THE WATER-TO-FEED RATIO (WFR) TREND
        // =========================================================================
        double totalFeed3Days = day1.getFeedQuantityUsed() + day2.getFeedQuantityUsed() + day3.getFeedQuantityUsed();
        double totalWater3Days = (day1.getWaterQuantityUsed() != null ? day1.getWaterQuantityUsed() : 0) +
                (day2.getWaterQuantityUsed() != null ? day2.getWaterQuantityUsed() : 0) +
                (day3.getWaterQuantityUsed() != null ? day3.getWaterQuantityUsed() : 0);

        if (totalFeed3Days > 0 && totalWater3Days > 0) {
            double rollingWfr = totalWater3Days / totalFeed3Days;

            // Standard ratio is ~2.0 (2 Liters water to 1 KG feed). If it drops below 1.5, metabolic crisis is imminent.
            if (rollingWfr < 1.5) {
                String message = String.format(
                        "🚨 HEALTH RISK: Acute Water Dehydration. The 72-hour Water-to-Feed Ratio has dropped to %.2f (Standard target is 2.0). " +
                                "When water drops faster than feed, it indicates immediate water line pressure blocks, nipple clogging, or acute disease onset.",
                        rollingWfr
                );
                alertService.triggerInternalAlert(activeBatch, currentLog, AlertType.CLIMATE_STRESS, message);
            }
        }

        // =========================================================================
        // 2. ANALYSIS PATH B: LABOR AND MANAGEMENT ERRACTICISM (COEFFICIENT OF VARIANCE)
        // =========================================================================
        double avgFeed = totalFeed3Days / 3.0;
        double feedVariance = (Math.pow(day1.getFeedQuantityUsed() - avgFeed, 2) +
                Math.pow(day2.getFeedQuantityUsed() - avgFeed, 2) +
                Math.pow(day3.getFeedQuantityUsed() - avgFeed, 2)) / 3.0;
        double feedVolatility = (Math.sqrt(feedVariance) / avgFeed) * 100.0;

        if (feedVolatility > 18.0) {
            String message = String.format(
                    "⚠️ MANAGEMENT INCONSISTENCY: High operational variance (%.1f%% volatility) detected across the last 3 days. " +
                            "Day 1: %.1fkg, Day 2: %.1fkg, Day 3: %.1fkg. This swinging pattern suggests uneven feeder filling, irregular lighting adjustments, or unstable labor schedules.",
                    feedVolatility, day1.getFeedQuantityUsed(), day2.getFeedQuantityUsed(), day3.getFeedQuantityUsed()
            );
            alertService.triggerInternalAlert(activeBatch, currentLog, AlertType.FEED_UNDER_CONSUMPTION, message);
        }

        // =========================================================================
        // 3. ANALYSIS PATH C: ECONOMIC DRIFT & WEIGHT VELOCITY
        // =========================================================================
        if (day3.getAverageWeight() != null && day3.getAverageWeight() > 0 && standard.getExpectedBodyWeight() != null) {
            double currentWeight = day3.getAverageWeight();
            double targetWeight = standard.getExpectedBodyWeight();

            // If feed consumption is normal or high, but weight is lagging behind the target by more than 15%
            if (currentWeight < (targetWeight * 0.85)) {
                String message = String.format(
                        "💰 ECONOMIC DRIFT ALERT: Low Feed Conversion Efficiency. Active weight is %dg, which lags 15%% behind the breed target (%dg). " +
                                "The birds are consuming feed expense but failing to convert it to meat/egg biomass. Check feed storage moisture levels or nutritional profile accuracy.",
                        currentWeight, targetWeight
                );
                alertService.triggerInternalAlert(activeBatch, currentLog, AlertType.PRODUCTION_YIELD_DROP, message);
            }
        }

        // =========================================================================
        // 4. ANALYSIS PATH D: CUMULATIVE MORTALITY VELOCITY (EXPONENTIAL SPREAD)
        // =========================================================================
        int m1 = day1.getMortalityCount();
        int m2 = day2.getMortalityCount();
        int m3 = day3.getMortalityCount();

        // Check for continuous multiplication acceleration (e.g., 1 -> 3 -> 8 deaths)
        if (m3 > m2 && m2 > m1 && m1 >= 0) {
            int totalDeathsWindow = m1 + m2 + m3;
            String message = String.format(
                    "🚨 EXPONENTIAL BIORISK: Accelerated Mortality Velocity detected. Daily deaths are compounding consecutively: Day 1: %d, Day 2: %d, Day 3: %d. " +
                            "Total losses this window: %d. This escalating infection curve suggests an active disease vector (e.g., Newcastle or Gumboro). Isolate section and initiate quarantine.",
                    m1, m2, m3, totalDeathsWindow
            );
            alertService.triggerInternalAlert(activeBatch, currentLog, AlertType.MORTALITY_LIMIT_BREACH, message);
        }
    }
}