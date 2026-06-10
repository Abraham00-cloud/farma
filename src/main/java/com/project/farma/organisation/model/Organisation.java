package com.project.farma.organisation.model;

import com.project.farma.farm.model.Farm;
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
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @Enumerated(EnumType.STRING)
    private OrganisationType organisationType;

//    @OneToOne(mappedBy = "organisation", cascade = CascadeType.ALL)
//    private Wallet wallet;

    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL)
    private List<User> members;

    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL)
    private List<Farm> farms;

    private String registrationNumber;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
