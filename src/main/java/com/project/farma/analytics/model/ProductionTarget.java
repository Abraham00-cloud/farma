package com.project.farma.analytics.model;

import com.project.farma.batch.model.Batch;
import com.project.farma.batch.model.Breed;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductionTarget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false, unique = true)
    private Batch batch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Breed breed;

    @Column(name = "target_fcr", nullable = false)
    private Double targetFcr;

    @Column(name = "target_weeks", nullable = false)
    private Integer targetWeeks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
