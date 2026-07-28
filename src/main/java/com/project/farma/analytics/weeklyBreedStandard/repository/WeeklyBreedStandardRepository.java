package com.project.farma.analytics.weeklyBreedStandard.repository;

import com.project.farma.batch.model.Breed;
import com.project.farma.analytics.weeklyBreedStandard.model.WeeklyBreedStandard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeeklyBreedStandardRepository extends JpaRepository<WeeklyBreedStandard, Long> {

    Optional<WeeklyBreedStandard> findByBreedAndWeekNumber(Breed breed, Integer weekNumber);

    boolean existsByBreed(Breed breed);
}
