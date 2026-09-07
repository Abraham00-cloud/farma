package com.project.farma.batch.controller;

import com.project.farma.batch.dto.*;
import com.project.farma.batch.model.Status;
import com.project.farma.batch.service.BatchService;
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
@RequestMapping("/api/v1/batches")
@Tag(
        name = "6. Batch Lifecycle Management",
        description = "Secure operational endpoints to initiate production cycles, track animal populations, and audit mortality indices"
)
public class BatchController {
    private final BatchService batchService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Initiate New Livestock Batch")
    public ResponseEntity<BatchResponseDto> createBatch(@Valid @RequestBody BatchRequestDto requestDto) {
        BatchResponseDto batch = batchService.createBatch(requestDto);
        return new ResponseEntity<>(batch, HttpStatus.CREATED);
    }

    @PatchMapping("/{batchId}/mortality")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Log Batch Mortality Event")
    public ResponseEntity<Void> updateBatchMortality(@PathVariable Long batchId, @RequestParam Integer deathCount) {
        batchService.updateBatchMortality(batchId, deathCount);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/{batchId}/close")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Close/Harvest a batch, release section housing, and finalize financials")
    public ResponseEntity<BatchCloseResponseDto> closeBatch(
            @PathVariable Long batchId,
            @Valid @RequestBody BatchCloseRequestDto closeDto
    ) {
        return ResponseEntity.ok(batchService.closeBatch(batchId, closeDto));
    }

    @PatchMapping("/{batchId}/partial-sale")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Record a partial sale")
    public ResponseEntity<Void> recordPartialSale(
            @PathVariable Long batchId,
            @Valid @RequestBody PartialSaleRequestDto requestDto) {
        batchService.recordPartialSale(batchId, requestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{batchId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch Individual Batch Details")
    public ResponseEntity<BatchResponseDto> getBatchDetailsById(@PathVariable Long batchId) {
        BatchResponseDto batch = batchService.getBatchDetailsById(batchId);
        return new ResponseEntity<>(batch, HttpStatus.OK);
    }

    @GetMapping("/section/{sectionId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch All Batches for a Section")
    public ResponseEntity<Page<BatchResponseDto>> getBatchesBySectionId(
            @PathVariable Long sectionId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BatchResponseDto> batches = batchService.getBatchesBySectionId(sectionId, pageable);
        return new ResponseEntity<>(batches, HttpStatus.OK);
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch All Batches for a Farm")
    public ResponseEntity<Page<BatchResponseDto>> getBatchesByFarmId(
            @PathVariable Long farmId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BatchResponseDto> batches = batchService.getBatchesByFarmId(farmId, pageable);
        return new ResponseEntity<>(batches, HttpStatus.OK);
    }
}