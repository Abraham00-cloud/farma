package com.project.farma.user.repository;

import com.project.farma.user.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(@Email(message = "Valid email is required") @NotBlank(message = "Email is required") String email);

    List<User> findAllByParentId(Long proprietorID);

    Optional<User> findByEmail(@Email(message = "Valid email is required") @NotBlank(message = "Email is required") String email);
}
