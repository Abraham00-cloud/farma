package com.project.farma.user.controller;

import com.project.farma.security.FarmUserPrincipal;
import com.project.farma.user.dto.UserResponseDto;
import com.project.farma.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(
        name = "2. User Management",
        description = "Secure endpoints to manage corporate farm profiles, roles, and hierarchies")

public class UserController {
    private final UserService userService;
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Get Managers by Proprietor",
            description = "Fetches a collection of all operational Managers linked directly underneath a specific enterprise Proprietor."
    )

    @GetMapping("/proprietor/{proprietorId}")
    public ResponseEntity<Page<UserResponseDto>> getManagersByProprietor(
            @PathVariable Long proprietorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long currentUserId = ((FarmUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<UserResponseDto> managers = userService.getUsersByProprietor(proprietorId, currentUserId, pageable);
        return new ResponseEntity<>(managers, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Get User Profile Details by ID")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long userId) {
        Long currentOrgId = ((FarmUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getOrganisationId();

        UserResponseDto user = userService.getUserById(userId, currentOrgId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }



    @PreAuthorize("hasRole('PROPRIETOR')")
    @Operation(
            summary = "Deactivate / Soft Delete Manager",
            description = "Revokes access for a manager without destroying their historical daily log records."
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deactivateUser(@PathVariable Long userId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = ((FarmUserPrincipal) principal).getId();

        userService.deactivateUser(userId, currentUserId);
        return ResponseEntity.ok(Map.of("message", "User has been successfully deactivated."));
    }

}