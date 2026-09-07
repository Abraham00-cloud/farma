package com.project.farma.batch.repository;

import com.project.farma.batch.model.Batch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    // 1. For External API (Paginated DTOs)
    Page<Batch> findBySectionFarmId(Long farmId, Pageable pageable);
    Page<Batch> findBySectionId(Long sectionId, Pageable pageable);

    // 2. For Internal Analytics & Finance (Flat Lists of Entities)
    List<Batch> findBySectionFarmId(Long farmId);
    List<Batch> findBySectionId(Long sectionId);
}