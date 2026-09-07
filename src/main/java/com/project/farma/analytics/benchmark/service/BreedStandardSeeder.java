package com.project.farma.analytics.benchmark.service;

import com.project.farma.batch.model.Breed;
import com.project.farma.analytics.benchmark.model.WeeklyBreedStandard;
import com.project.farma.analytics.benchmark.repository.WeeklyBreedStandardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BreedStandardSeeder implements CommandLineRunner {
    private final WeeklyBreedStandardRepository repository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!repository.existsByBreed(Breed.COBB_500 )) {
            log.info("Seeding static reference benchmarks for COBB_500 Broiler standards...");
            seedCobb500Standards();
        }
        if (!repository.existsByBreed(Breed.ISA_BROWN)) {
            log.info("Seeding static reference benchmarks for ISA_BROWN Layer standards...");
            seedIsaBrownStandards();
        }
    }

    private void seedCobb500Standards() {
        List<WeeklyBreedStandard> standards = new ArrayList<>();

        standards.add(new WeeklyBreedStandard(null, Breed.COBB_500, 1, 0.20, 25.0, null, 0.0040));
        standards.add(new WeeklyBreedStandard(null, Breed.COBB_500, 2, 0.48, 56.0, null, 0.0035));
        standards.add(new WeeklyBreedStandard(null, Breed.COBB_500, 3, 0.93, 91.0, null, 0.0035));
        standards.add(new WeeklyBreedStandard(null, Breed.COBB_500, 4, 1.55, 128.0, null, 0.0030));
        standards.add(new WeeklyBreedStandard(null, Breed.COBB_500, 5, 2.26, 162.0, null, 0.0030));
        standards.add(new WeeklyBreedStandard(null, Breed.COBB_500, 6, 2.98, 191.0, null, 0.0030));
        standards.add(new WeeklyBreedStandard(null, Breed.COBB_500, 7, 3.65, 212.0, null, 0.0030));

        repository.saveAll(standards);
        log.info("Successfully loaded 7 weeks of Broiler targets into the master dictionary.");
    }

    private void seedIsaBrownStandards() {
        List<WeeklyBreedStandard> standards = new ArrayList<>();

        standards.add(new WeeklyBreedStandard(null, Breed.ISA_BROWN, 18, 1.55, 85.0, 5.0, 0.0010));
        standards.add(new WeeklyBreedStandard(null, Breed.ISA_BROWN, 20, 1.70, 100.0, 45.0, 0.0010));
        standards.add(new WeeklyBreedStandard(null, Breed.ISA_BROWN, 22, 1.82, 112.0, 88.0, 0.0010));
        standards.add(new WeeklyBreedStandard(null, Breed.ISA_BROWN, 24, 1.88, 116.0, 93.5, 0.0010));
        standards.add(new WeeklyBreedStandard(null, Breed.ISA_BROWN, 26, 1.91, 118.0, 94.0, 0.0010));

        repository.saveAll(standards);
        log.info("Successfully loaded initial Layer production metrics into the master dictionary.");
    }




}
