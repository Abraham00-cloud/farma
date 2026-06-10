package com.project.farma.user.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(
        name = "1. Authentication Gateway",
        description = "Public endpoints for user authentication and provisioning access tokens"
)

public class AuthController {
    private final UserService userService;

    @Operation(
            summary = "Create a New User",
            description = "Provides a brand new profile (Proprietor or Manager) and binds them securely to their designated multi-tenant organization boundary."
    )
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto createdUser = userService.createUser(userRequestDto);
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

}
