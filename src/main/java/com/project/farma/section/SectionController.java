package com.project.farma.section;


import com.project.farma.farm.dto.FarmResponseDto;
import com.project.farma.farm.model.Farm;
import com.project.farma.section.dto.SectionRequestDto;
import com.project.farma.section.dto.SectionResponseDto;
import com.project.farma.section.model.Section;
import com.project.farma.section.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sections")
@Tag(
        name = "5. Section Management",
        description = "Secure endpoints to manage physical holding pens, housing sectors, and layout configurations inside farm facilities"
)
public class SectionController {
    private final SectionService sectionService;

    @PostMapping
    @PreAuthorize("hasRole('PROPRIETOR')")
    @Operation(
            summary = "Create a Farm Housing Section",
            description = "Allows an authorized Proprietor to map a brand new holding area (e.g., Pen, Brooder, Coops) inside a specific farm boundary."
    )
    public ResponseEntity<SectionResponseDto> createSection(@Valid @RequestBody SectionRequestDto requestDto) {
        SectionResponseDto section = sectionService.createSection(requestDto);
        return new ResponseEntity<>(section, HttpStatus.OK);
    }

    @PutMapping("/{sectionId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Update Structural Section Configs",
            description = "Modifies capacity constraints, production targets, or animal category definitions for an existing farm division structure."
    )
    public ResponseEntity<SectionResponseDto> updateSection(@PathVariable Long sectionId,  @RequestBody SectionRequestDto requestDto) {
        SectionResponseDto updatedSection = sectionService.updateSection(sectionId, requestDto);
        return new ResponseEntity<>(updatedSection, HttpStatus.OK);
    }


    @PatchMapping("/{sectionId}/status")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Toggle Section Operational Availability",
            description = "Updates the allocation state (available/quarantined/full) of a farm house section to toggle if new animal batches can be introduced."
    )
    public ResponseEntity<Void> setSectionStatus(@PathVariable Long sectionId,@RequestParam boolean status) {
        sectionService.setSectionStatus(sectionId, status);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Fetch All Sections in a Farm",
            description = "Compiles a exhaustive list of all housing structures registered under a single physical farm installation."
    )
    public ResponseEntity<List<SectionResponseDto>> getSectionsByFarm(@PathVariable Long farmId) {
       List<SectionResponseDto> sections = sectionService.getSectionsByFarm(farmId);
       return new ResponseEntity<>(sections, HttpStatus.OK);
    }


    @GetMapping("/farm/{farmId}/available")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Fetch Available Empty Housing Slots",
            description = "Filters and displays only vacant or structurally receptive housing environments inside a farm layout."
    )
    public ResponseEntity<List<SectionResponseDto>> getAvailableSectionsByFarm(@PathVariable Long farmId) {
        List<SectionResponseDto> sections = sectionService.getAvailableSectionsByFarm(farmId);
        return new ResponseEntity<>(sections, HttpStatus.OK);
    }


    @GetMapping("/{sectionId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Fetch Individual Section Details",
            description = "Locates and returns structural identity metrics, capacity configurations, and status data for a specific farm housing section."
    )
    public ResponseEntity<SectionResponseDto> getSectionDetailsById(@PathVariable Long sectionId) {
        SectionResponseDto section = sectionService.getSectionDetailsById(sectionId);
        return new ResponseEntity<>(section, HttpStatus.OK);
    }
}
