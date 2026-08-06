package com.project.farma.dailyLogs.controller;

import com.project.farma.dailyLogs.dto.DailyLogRequestDto;
import com.project.farma.dailyLogs.dto.DailyLogResponseDto;
import com.project.farma.dailyLogs.mapper.DailyLogMapper;
import com.project.farma.dailyLogs.service.DailyLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/daily-logs")
@RequiredArgsConstructor
@Tag(
        name = "7. Daily Logs Management",
        description = "Operational endpoints to capture daily farm entries, execute inventory rollbacks, and record cost balances"
)
public class DailyLogs {
    private final DailyLogService dailyLogService;
    private final DailyLogMapper dailyLogMapper;

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


    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch Telemetry Logs for a Specific Batch")
    public ResponseEntity<List<DailyLogResponseDto>> getLogsForBatch(@PathVariable Long batchId) {
        List<DailyLogResponseDto> logs = dailyLogService.getLogsForBatch(batchId)
                .stream()
                .map(dailyLogMapper::toDailyLogResponseDto)
                .toList();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/batch/{batchId}/window")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch Telemetry Logs for a Batch within Date Window")
    public ResponseEntity<List<DailyLogResponseDto>> getLogsForBatchInWindow(
            @PathVariable Long batchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<DailyLogResponseDto> logs = dailyLogService.getLogsForBatchInWindow(batchId, startDate, endDate)
                .stream()
                .map(dailyLogMapper::toDailyLogResponseDto)
                .toList();
        return ResponseEntity.ok(logs);
    }



}
