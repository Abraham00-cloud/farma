package com.project.farma.section.controller;

import com.project.farma.section.dto.SectionRequestDto;
import com.project.farma.section.dto.SectionResponseDto;
import com.project.farma.section.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Create a Farm Housing Section")
    public ResponseEntity<SectionResponseDto> createSection(@Valid @RequestBody SectionRequestDto requestDto) {
        SectionResponseDto section = sectionService.createSection(requestDto);
        return new ResponseEntity<>(section, HttpStatus.CREATED);
    }

    @PutMapping("/{sectionId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Update Structural Section Configs")
    public ResponseEntity<SectionResponseDto> updateSection(
            @PathVariable Long sectionId,
            @Valid @RequestBody SectionRequestDto requestDto
    ) {
        SectionResponseDto updatedSection = sectionService.updateSection(sectionId, requestDto);
        return new ResponseEntity<>(updatedSection, HttpStatus.OK);
    }

    @PatchMapping("/{sectionId}/status")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Toggle Section Operational Availability")
    public ResponseEntity<Void> setSectionStatus(@PathVariable Long sectionId, @RequestParam boolean status) {
        sectionService.setSectionStatus(sectionId, status);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch All Sections in a Farm")
    public ResponseEntity<Page<SectionResponseDto>> getSectionsByFarm(
            @PathVariable Long farmId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<SectionResponseDto> sections = sectionService.getSectionsByFarm(farmId, pageable);
        return new ResponseEntity<>(sections, HttpStatus.OK);
    }

    @GetMapping("/farm/{farmId}/available")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch Available Empty Housing Slots")
    public ResponseEntity<Page<SectionResponseDto>> getAvailableSectionsByFarm(
            @PathVariable Long farmId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<SectionResponseDto> sections = sectionService.getAvailableSectionsByFarm(farmId, pageable);
        return new ResponseEntity<>(sections, HttpStatus.OK);
    }

    @GetMapping("/{sectionId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch Individual Section Details")
    public ResponseEntity<SectionResponseDto> getSectionDetailsById(@PathVariable Long sectionId) {
        SectionResponseDto section = sectionService.getSectionDetailsById(sectionId);
        return new ResponseEntity<>(section, HttpStatus.OK);
    }
}