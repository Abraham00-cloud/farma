package com.project.farma.batch.controller;

import com.project.farma.batch.dto.BatchRequestDto;
import com.project.farma.batch.dto.BatchResponseDto;
import com.project.farma.batch.model.Batch;
import com.project.farma.batch.model.Status;
import com.project.farma.batch.service.BatchService;
import com.project.farma.section.model.Section;
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
@RequestMapping("/api/v1/batches")
@Tag(
        name = "6. Batch Lifecycle Management",
        description = "Secure operational endpoints to initiate production cycles, track animal populations, and audit mortality indices"

)
public class BatchController {
    private final BatchService batchService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Initiate New Livestock Batch",
            description = "Spins up an active production cycle tracking context for a flock/herd group within an unallocated farm housing section. This action auto-locks the targeted section's availability status."
    )
    public ResponseEntity<BatchResponseDto> createBatch(@Valid @RequestBody BatchRequestDto requestDto) {
        BatchResponseDto batch = batchService.createBatch(requestDto);
        return new ResponseEntity<>(batch, HttpStatus.CREATED);
    }

    @PatchMapping("/{batchId}/mortality")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Log Batch Mortality Event",
            description = "Increments total historical mortalities and down-adjusts the live current herd inventory balance based on recorded daily casualty counts."
    )
    public ResponseEntity<Void> updateBatchMortality(@PathVariable Long batchId, @RequestParam Integer deathCount) {
        batchService.updateBatchMortality(batchId, deathCount);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{batchId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Fetch Individual Batch Details",
            description = "Retrieves live operational metrics, calculated batch codes, tracking logs, and current inventory levels for a specific livestock lifecycle cohort."
    )
    public ResponseEntity<BatchResponseDto> getBatchDetailsById(@PathVariable Long batchId) {
        BatchResponseDto batch = batchService.getBatchDetailsById(batchId);
        return new ResponseEntity<>(batch, HttpStatus.OK);
    }

}
