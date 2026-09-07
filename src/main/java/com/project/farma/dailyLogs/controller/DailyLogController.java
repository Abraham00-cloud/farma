package com.project.farma.dailyLogs.controller;

import com.project.farma.dailyLogs.dto.DailyLogRequestDto;
import com.project.farma.dailyLogs.dto.DailyLogResponseDto;
import com.project.farma.dailyLogs.mapper.DailyLogMapper;
import com.project.farma.dailyLogs.service.DailyLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/daily-logs")
@RequiredArgsConstructor
@Tag(
        name = "7. Daily Logs Management",
        description = "Operational endpoints to capture daily farm entries, execute inventory rollbacks, and record cost balances"
)
public class DailyLogController {
    private final DailyLogService dailyLogService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Create Daily Operational Log",
            description = "Saves the daily record for a specific batch. This action auto-decrements material stock records and triggers cost accounting entries for feed or medicine consumed."
    )
    public ResponseEntity<DailyLogResponseDto> createDailyLog(@Valid @RequestBody DailyLogRequestDto requestDto) {
        DailyLogResponseDto dailyLog = dailyLogService.createDailyLog(requestDto);
        return new ResponseEntity<>(dailyLog, HttpStatus.CREATED);
    }



    @PutMapping("/{logId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Update an existing daily log", description = "Safely reverses old inventory deductions and ledger entries before applying the new telemetry data.")
    public ResponseEntity<DailyLogResponseDto> updateDailyLog(
            @PathVariable Long logId,
            @Valid @RequestBody DailyLogRequestDto requestDto) {
        DailyLogResponseDto updatedLog = dailyLogService.updateDailyLog(logId, requestDto);
        return new ResponseEntity<>(updatedLog, HttpStatus.OK);
    }

    @DeleteMapping("/{logId}")
    @PreAuthorize("hasRole('PROPRIETOR')")
    @Operation(summary = "Delete a daily log", description = "Deletes a log, restores mortality, refunds inventory, and credits the financial ledger.")
    public ResponseEntity<Void> deleteDailyLog(@PathVariable Long logId) {
        dailyLogService.deleteDailyLog(logId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch Telemetry Logs for a Specific Batch")
    public ResponseEntity<Page<DailyLogResponseDto>> getLogsForBatch(
            @PathVariable Long batchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("logDate").descending());
        Page<DailyLogResponseDto> logs = dailyLogService.getLogsForBatch(batchId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/batch/{batchId}/window")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch Telemetry Logs for a Batch within Date Window")
    public ResponseEntity<Page<DailyLogResponseDto>> getLogsForBatchInWindow(
            @PathVariable Long batchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("logDate").descending());
        Page<DailyLogResponseDto> logs = dailyLogService.getLogsForBatchInWindow(batchId, startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }



}
