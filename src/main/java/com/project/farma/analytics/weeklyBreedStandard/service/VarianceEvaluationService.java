package com.project.farma.analytics.weeklyBreedStandard.service;

import com.project.farma.analytics.systemAlert.model.AlertType;
import com.project.farma.analytics.systemAlert.service.SystemAlertService;
import com.project.farma.analytics.weeklyBreedStandard.model.WeeklyBreedStandard;
import com.project.farma.batch.model.Batch;
import com.project.farma.dailyLogs.model.DailyLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VarianceEvaluationService implements DailyLogEvaluator {

    private final SystemAlertService alertService;

    @Override
    public void evaluate(DailyLog currentLog, Batch activeBatch, WeeklyBreedStandard breedStandard, Map<String, Double> climate) {
        int currentHeadcount = activeBatch.getCurrentCount();
        if (currentHeadcount <= 0) {
            log.warn("Evaluation bypassed: Operational headcount for Batch {} is zero.", activeBatch.getBatchNumber());
            return;
        }

        double ambientTemp = climate.get("temperature");
        double relativeHumidity = climate.get("humidity");

        // 1. MATHEMATICAL TEMPERATURE-HUMIDITY INDEX (THI) CALCULATION & ALERT TRIGGER
        double thi = ambientTemp + (0.36 * relativeHumidity) + 41.5;
        log.info("Computed Microclimate Stress Index (THI) for Batch [{}]: {}", activeBatch.getBatchNumber(), String.format("%.2f", thi));

        // Proactive Threshold Trigger: If THI crosses the critical heat exhaustion index limit (> 84.0)
        if (thi > 84.0) {
            String climateMessage = String.format(
                    "CRITICAL CLIMATE STRESS: Ambient Temperature is %.1f°C with %.1f%% Humidity (THI: %.2f). " +
                            "The pen environment has crossed safe thermal biological limits. Immediately check fans, wetting systems, or open insulation side-curtains.",
                    ambientTemp, relativeHumidity, thi
            );
            alertService.triggerInternalAlert(activeBatch, currentLog, AlertType.CLIMATE_STRESS, climateMessage);
        }

        // 2. BACKGROUND MORTALITY THRESHOLD CHECKS
        double acceptableDailyDeathsLimit = (breedStandard.getMaxWeeklyMortalityRate() / 7.0) * currentHeadcount;
        int reportedDeathsToday = currentLog.getMortalityCount();

        if (reportedDeathsToday > acceptableDailyDeathsLimit) {
            String message;
            if (thi > 82.0) {
                message = String.format("🚨 BIORISK ALERT: Mortality spike of %d deaths detected under acute heat stress (THI: %.2f). Immediate cooling required.", reportedDeathsToday, thi);
            } else {
                message = String.format("🚨 BIORISK ALERT: High mortality breach (%d deaths) detected under comfortable climate conditions. Potential disease threat vector.", reportedDeathsToday);
            }
            alertService.triggerInternalAlert(activeBatch, currentLog, AlertType.MORTALITY_LIMIT_BREACH, message);
        }
    }
}