package com.project.farma.batch.repository;

import com.project.farma.batch.model.Batch;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Long> {
    List<Batch> findBySectionFarmId(Long farmId);

    List<Batch> findBySectionId(Long sectionId);

}
