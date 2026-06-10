package com.project.farma.section.repository;

import com.project.farma.section.dto.SectionResponseDto;
import com.project.farma.section.model.Section;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    boolean existsByNameAndFarmId(@NotBlank(message = "Section Name is required") String name, @NotNull(message = "FarmId is required" ) Long farmId);

    List<Section> findAllByFarmId(Long farmId);

    List<Section> findAllByFarmIdAndIsAvailableTrue(Long farmId);
}
