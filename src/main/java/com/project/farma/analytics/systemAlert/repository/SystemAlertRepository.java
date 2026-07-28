package com.project.farma.analytics.systemAlert.repository;

import com.project.farma.analytics.systemAlert.model.AlertStatus;
import com.project.farma.analytics.systemAlert.model.AlertType;
import com.project.farma.analytics.systemAlert.model.SystemAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SystemAlertRepository extends JpaRepository<SystemAlert, Long> {
    List<SystemAlert> findByBatchIdAndStatus(Long batchId, AlertStatus status);

    boolean existsByBatchIdAndAlertTypeAndStatusIn(Long batchId, AlertType type, Collection<AlertStatus> statuses);

    List<SystemAlert> findByBatchIdAndStatusIn(Long batchId, List<AlertStatus> triggered);

    long countByBatchIdAndStatusIn(Long batchId, List<AlertStatus> triggered);

    long countByBatchIdAndStatus(Long batchId, AlertStatus alertStatus);
}
