package com.project.farma.user.model;

import com.project.farma.farm.model.Farm;
import com.project.farma.organisation.model.Organisation;
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
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    @ManyToOne
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private User parent;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "manager")
    private List<Farm> managedFarms;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

}
