package com.project.farma.section.model;

import com.project.farma.batch.model.Batch;
import com.project.farma.farm.model.Farm;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    private List<Batch> batches;

    @Enumerated(EnumType.STRING)
    private AnimalCategory animalCategory;

    @Enumerated(EnumType.STRING)
    private ProductionType productionType;

    private Integer capacity;

    private boolean isAvailable;
}
