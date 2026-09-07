package com.project.farma.analytics.benchmark.model;


import com.project.farma.batch.model.Breed;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "weekly_breed_standards",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"breed", "week_number"})}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyBreedStandard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Breed breed;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

    @Column(name = "expected_body_weight")
    private Double expectedBodyWeight;

    @Column(name = "expected_daily_feed_intake", nullable = false)
    private Double expectedDailyFeedIntake;

    @Column(name = "expected_production_rate")
    private Double expectedProductionRate;

    @Column(name = "max_weekly_mortality_rate", nullable = false)
    private Double maxWeeklyMortalityRate;
}
