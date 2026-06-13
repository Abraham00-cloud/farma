package com.project.farma.farm.controller;

import com.project.farma.farm.dto.FarmRequestDto;
import com.project.farma.farm.dto.FarmResponseDto;
import com.project.farma.farm.model.Farm;
import com.project.farma.farm.service.FarmService;
import com.project.farma.organisation.model.Organisation;
import com.project.farma.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/farms")
@Tag(
        name = "4. Farm Management",
        description = "Secure endpoints to provision physical farm hubs and manage assigned supervisors"
)
public class FarmController {
    private final FarmService farmService;

    @PostMapping
    @PreAuthorize("hasRole('PROPRIETOR')")
    @Operation(
            summary = "Create a Physical Farm Hub",
            description = "Allows an authenticated Proprietor to provision a physical farm sector and bind an operational Manager to it within their organization tenant."
    )
    public ResponseEntity<FarmResponseDto> createFarm(@Valid @RequestBody FarmRequestDto requestDto) {
       FarmResponseDto createdFarm = farmService.createFarm(requestDto);
       return new ResponseEntity<>(createdFarm, HttpStatus.CREATED);
    }

    @GetMapping("/organisation/{organisationId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Get All Farms in an Organisation",
            description = "Compiles a complete list of all operational farm installations assigned under a specific corporate multi-tenant ID."
    )
    public ResponseEntity<List<FarmResponseDto>> getFarmsByOrganisation(@PathVariable Long organisationId) {
        List<FarmResponseDto> farms = farmService.getFarmsByOrganisation(organisationId);
        return new ResponseEntity<>(farms, HttpStatus.OK);
    }

    @GetMapping("/{farmId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Fetch Individual Farm Details",
            description = "Locates and returns structural entity details for a specific farm using its primary database identifier."
    )
    public ResponseEntity<FarmResponseDto> getFarmDetailsById(@PathVariable Long farmId) {
        FarmResponseDto farm = farmService.getFarmDetailsById(farmId);
        return new ResponseEntity<>(farm, HttpStatus.OK);
    }
}
