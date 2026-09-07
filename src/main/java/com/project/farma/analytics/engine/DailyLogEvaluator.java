package com.project.farma.analytics.engine;

import com.project.farma.analytics.benchmark.model.WeeklyBreedStandard;
import com.project.farma.batch.model.Batch;
import com.project.farma.dailyLogs.model.DailyLog;

import java.util.Map;

public interface DailyLogEvaluator {
    void evaluate(DailyLog log, Batch batch, WeeklyBreedStandard standard, Map<String, Double> environment);
}
