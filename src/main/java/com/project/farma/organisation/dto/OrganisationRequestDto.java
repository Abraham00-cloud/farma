package com.project.farma.organisation.dto;

import com.project.farma.organisation.model.OrganisationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrganisationRequestDto(
        @NotBlank(message = "Organisation name is required")
        String name,
        @NotNull(message = "Organisation type is required")
        OrganisationType organisationType,
        @NotBlank(message = "email is required")
        @Email(message = "Valid email is required")
        String email,
        @NotBlank(message = "Registration number is required")
        String registrationNumber,
        @NotBlank(message = "Admin first name is required")
        String adminFirstName,
        @NotBlank(message = "Admin last name is required")
        String adminLastName,
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password


) {
}
