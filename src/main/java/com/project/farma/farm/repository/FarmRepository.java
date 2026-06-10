package com.project.farma.farm.repository;

import com.project.farma.farm.model.Farm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmRepository extends JpaRepository<Farm, Long> {


    List<Farm> findAllByOrganisationId(Long organisationId);

    boolean existsByNameAndOrganisationId(@NotBlank(message = "Farm name is required") String name, @NotNull(message = "Organisation id is required") Long organisationId);
}
