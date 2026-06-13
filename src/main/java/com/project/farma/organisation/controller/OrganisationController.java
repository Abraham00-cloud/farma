package com.project.farma.organisation.controller;

import com.project.farma.organisation.dto.OrganisationRequestDto;
import com.project.farma.organisation.dto.OrganisationResponseDto;
import com.project.farma.organisation.service.OrganisationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organisations")
@Tag(
        name = "3. Organisation Management",
        description = "Endpoints for multi-tenant enterprise onboarding and corporate profile inspection"
)
public class OrganisationController {
    private final OrganisationService organisationService;

    @PostMapping
    @Operation(
            summary = "Register New Organisation",
            description = "Public endpoint that registers a brand new corporate agribusiness entity and automatically provisions its root Proprietor profile inside a single transaction."
    )
    public ResponseEntity<OrganisationResponseDto> createOrganisation(@Valid @RequestBody OrganisationRequestDto requestDto) {
        OrganisationResponseDto organisation = organisationService.createOrganisation(requestDto);
        return new ResponseEntity<>(organisation, HttpStatus.CREATED);
    }

    @GetMapping("/{organisationId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Fetch Organisation Profile",
            description = "Retrieves the core operational details and registration parameters of a corporate tenant boundary by its primary identifier."
    )
    public ResponseEntity<OrganisationResponseDto> getOrganisationDetails(@PathVariable Long organisationId) {
        OrganisationResponseDto organisation = organisationService.getOrganisationDetails(organisationId);
        return new ResponseEntity<>(organisation, HttpStatus.OK);
    }

}
