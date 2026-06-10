package com.project.farma.dailyLogs.repository;

import com.project.farma.dailyLogs.model.DailyLog;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

    boolean existsByBatchIdAndLogDate(Long batchId, LocalDate localDate);
}
