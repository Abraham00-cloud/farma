package com.project.farma.dailyLogs.model;

import com.project.farma.batch.model.Batch;
import com.project.farma.inventory.model.Inventory;
import com.project.farma.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inventory_id")
    private Inventory feedInventory;

    private LocalDate logDate;

    private Double feedQuantityUsed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_inventory_id")
    private Inventory medicineInventory;

    private Double medicineQuantityUsed;

    private String administrationMethod;


    private Integer mortalityCount;
    private Double averageWeight;

    private String observations;
    @ManyToOne
    @JoinColumn(name = "recorded_by_id", nullable = false)
    private User recordedBy;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @CreationTimestamp
    private LocalDateTime createdAt;




}
