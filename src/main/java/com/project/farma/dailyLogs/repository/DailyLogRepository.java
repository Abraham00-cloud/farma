package com.project.farma.dailyLogs.repository;

import com.project.farma.dailyLogs.model.DailyLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

    boolean existsByBatchIdAndLogDate(Long batchId, LocalDate localDate);

    // 1. For External API (Paginated DTOs)
    Page<DailyLog> findByBatchId(Long batchId, Pageable pageable);
    Page<DailyLog> findByBatchIdAndLogDateBetween(Long batchId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // 2. For Internal Analytics (Flat Lists of Entities)
    List<DailyLog> findByBatchId(Long batchId, Sort sort);
    List<DailyLog> findByBatchIdAndLogDateBetween(Long batchId, LocalDate startDate, LocalDate endDate, Sort sort);
}