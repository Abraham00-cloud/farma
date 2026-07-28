package com.project.farma.analytics.weeklyBreedStandard.service;

import com.project.farma.analytics.weeklyBreedStandard.model.WeeklyBreedStandard;
import com.project.farma.batch.model.Batch;
import com.project.farma.dailyLogs.model.DailyLog;

import java.util.Map;

public interface DailyLogEvaluator {
    void evaluate(DailyLog log, Batch batch, WeeklyBreedStandard standard, Map<String, Double> environment);
}
