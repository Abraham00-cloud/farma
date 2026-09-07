package com.project.farma.analytics.benchmark.service;

import com.project.farma.analytics.benchmark.model.WeeklyBreedStandard;
import com.project.farma.analytics.benchmark.repository.WeeklyBreedStandardRepository;
import com.project.farma.batch.model.Batch;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AnalyticsHelperService {
    private final WeeklyBreedStandardRepository weeklyBreedStandardRepository;

    public int calculateCurrentWeek(Batch batch, LocalDate logDate) {
        if (batch.getStartDate() == null) {
            throw new IllegalStateException("Batch start date is missing. Cannot calculate biological age.");
        }

        long daysElapsed = ChronoUnit.DAYS.between(batch.getStartDate(), logDate);

        if (daysElapsed < 0) {
            throw new IllegalArgumentException("Log date cannot be chronologically prior to the batch start date.");
        }

        return (int) (daysElapsed/7 + 1);
    }

    public WeeklyBreedStandard getTargetForBatchAtAge(Batch batch, LocalDate logDate) {
        int currentWeek = calculateCurrentWeek(batch, logDate);

        return weeklyBreedStandardRepository
                .findByBreedAndWeekNumber(batch.getBreed(), currentWeek)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Data missing: No seeded standard data found for breed %s at Week %d",
                                batch.getBreed(), currentWeek)
                ));
    }
}
