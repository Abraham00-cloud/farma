package com.project.farma.section.repository;

import com.project.farma.section.model.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    boolean existsByNameAndFarmId(String name, Long farmId);

    // FOR EXTERNAL API (Paginated)
    Page<Section> findAllByFarmId(Long farmId, Pageable pageable);
    Page<Section> findAllByFarmIdAndIsAvailableTrue(Long farmId, Pageable pageable);

    // FOR INTERNAL USE (Flat Lists)
    List<Section> findAllByFarmId(Long farmId);
}