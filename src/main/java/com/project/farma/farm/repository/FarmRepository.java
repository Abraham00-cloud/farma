package com.project.farma.farm.repository;

import com.project.farma.farm.model.Farm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmRepository extends JpaRepository<Farm, Long> {

    List<Farm> findAllByOrganisationId(Long organisationId);

    Page<Farm> findAllByOrganisationId(Long organisationId, Pageable pageable);

    boolean existsByNameAndOrganisationId(String name, Long organisationId);
}
