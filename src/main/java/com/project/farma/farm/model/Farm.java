package com.project.farma.farm.model;

import com.project.farma.batch.model.Batch;
import com.project.farma.organisation.model.Organisation;
import com.project.farma.section.model.Section;
import com.project.farma.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String address;

    @ManyToOne
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL)
    private List<Section> sections;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    private boolean isActive;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
