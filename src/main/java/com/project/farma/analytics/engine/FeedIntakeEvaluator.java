package com.project.farma.analytics.engine;

import com.project.farma.analytics.alert.model.AlertType;
import com.project.farma.analytics.alert.service.SystemAlertService;
import com.project.farma.analytics.benchmark.model.WeeklyBreedStandard;
import com.project.farma.batch.model.Batch;
import com.project.farma.dailyLogs.model.DailyLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedIntakeEvaluator implements DailyLogEvaluator {
    private final SystemAlertService alertService;
    private static final double STANDARD_BAG_WEIGHT_KG = 25.0;

    @Override
    public void evaluate(DailyLog logg, Batch batch, WeeklyBreedStandard standard, Map<String, Double> environment) {
        if (logg.getFeedQuantityUsed() == null || logg.getFeedQuantityUsed() <= 0) {
            return;
        }

        double temperature = environment.get("temperature");
        double humidity = environment.get("humidity");
        double thi = temperature + (0.36 * humidity) + 41.5;

        double expectedFlockFeedKg = (standard.getExpectedDailyFeedIntake() * batch.getCurrentCount()) / 1000.0;

        double actualFeedKg = logg.getFeedQuantityUsed() * STANDARD_BAG_WEIGHT_KG;

        double lowerBoundModifier = 0.90;

        if (thi > 78.0) {
            lowerBoundModifier = 0.82;
            log.info("Heat stress detected (THI: {}). Dynamically adjusted appetite lower bounds to -18%.", String.format("%.2f", thi));
        }

        if (actualFeedKg < (expectedFlockFeedKg * lowerBoundModifier)) {
            double driftPct = ((expectedFlockFeedKg - actualFeedKg) / expectedFlockFeedKg) * 100.0;
            String message = String.format(
                    "CRITICAL: Feed drop of %.2f%% detected. Current THI: %.2f. Action: Inspect for feeder clogging, water access, or heat distress.",
                    driftPct, thi
            );

            alertService.triggerInternalAlert(batch, logg, AlertType.FEED_UNDER_CONSUMPTION, message);
        }
    }
}