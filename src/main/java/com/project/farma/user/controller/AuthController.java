package com.project.farma.user.controller;

import com.project.farma.passwordReset.service.PasswordResetService;
import com.project.farma.security.FarmUserPrincipal;
import com.project.farma.user.dto.AuthResponseDto;
import com.project.farma.user.dto.LoginRequestDto;
import com.project.farma.user.dto.UserRequestDto;
import com.project.farma.user.dto.UserResponseDto;
import com.project.farma.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(
        name = "1. Authentication Gateway",
        description = "Public endpoints for user authentication, password recovery, and provisioning access tokens"
)
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @Operation(
            summary = "Create a New User",
            description = "Provides a brand new profile (Proprietor or Manager) and binds them securely to their designated multi-tenant organization boundary."
    )
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        Long currentUserId = null;

        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof FarmUserPrincipal) {
            currentUserId = ((FarmUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        }

        UserResponseDto createdUser = userService.createUser(userRequestDto, currentUserId);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(
            summary = "User Login Authentication",
            description = "Verifies user credentials against stored profiles and issues a secure multi-tenant JWT access token."
    )
    public ResponseEntity<AuthResponseDto> authenticate(@Valid @RequestBody LoginRequestDto requestDto) {
        AuthResponseDto authenticatedUser = userService.authenticate(requestDto);
        return new ResponseEntity<>(authenticatedUser, HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request Password Reset Instructions",
            description = "Triggers a secure, time-sensitive password recovery link to the user's email inbox without revealing user existence."
    )
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        passwordResetService.createPasswordResetTokenForUser(email);

        return ResponseEntity.ok(Map.of("message", "If an account matches this email, reset instructions have been dispatched."));
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Execute Password Reset",
            description = "Validates the active reset token and updates the user's password securely."
    )
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        boolean success = passwordResetService.resetPassword(token, newPassword);
        if (!success) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired password reset token."));
        }

        return ResponseEntity.ok(Map.of("message", "Password has been successfully updated. You may now sign in."));
    }
}