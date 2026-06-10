package com.project.farma.organisation.repository;

import com.project.farma.organisation.model.Organisation;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    boolean existsByEmail(@NotBlank(message = "Email is already in use")String email);

    boolean existsByRegistrationNumber(@NotBlank(message = "Registration is already in use") String registrationNumber);
}
