package com.project.farma.user.repository;

import com.project.farma.user.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(String email);

    Page<User> findAllByParentId(Long proprietorID, Pageable pageable);

    Optional<User> findByEmail( String email);
}
