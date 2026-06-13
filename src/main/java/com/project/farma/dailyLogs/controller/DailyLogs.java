package com.project.farma.dailyLogs.controller;

import com.project.farma.dailyLogs.dto.DailyLogRequestDto;
import com.project.farma.dailyLogs.dto.DailyLogResponseDto;
import com.project.farma.dailyLogs.service.DailyLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/daily-logs")
@RequiredArgsConstructor
@Tag(
        name = "7. Daily Logs Management",
        description = "Operational endpoints to capture daily farm entries, execute inventory rollbacks, and record cost balances"
)
public class DailyLogs {
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

}
