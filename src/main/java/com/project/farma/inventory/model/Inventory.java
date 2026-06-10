package com.project.farma.inventory.model;

import com.project.farma.farm.model.Farm;
import com.project.farma.organisation.model.Organisation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private InventoryCategory category;

    @Column(nullable = false)
    private Double currentQuantity;

    private String unit;

    @ManyToOne
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    private Double unitPrice;

    private LocalDate expiryDate;

    private double lowStockThreshold;



}
