package com.project.farma.transaction.model;

import com.project.farma.batch.model.Batch;
import com.project.farma.organisation.model.Organisation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionCategory category;

    @Column(nullable = false)
    private LocalDate transactionDate;

    private String description;

    private boolean isCashFlow;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    private LocalDateTime createdAt;


}
