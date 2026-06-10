package com.project.farma.user.controller;

import com.project.farma.user.dto.UserRequestDto;
import com.project.farma.user.dto.UserResponseDto;
import com.project.farma.user.model.User;
import com.project.farma.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(
        name = "2. User Management",
        description = "Secure endpoints to manage corporate farm profiles, roles, and hierarchies")
public class UserController {
    private final UserService userService;



    @Operation(
            summary = "Get Managers by Proprietor",
            description = "Fetches a collection of all operational Managers linked directly underneath a specific enterprise Proprietor."
    )
    @GetMapping("/proprietor/{proprietorId}")
    public ResponseEntity<List<UserResponseDto>> getManagersByProprietor(@PathVariable Long proprietorId) {
        List<UserResponseDto> managers = userService.getUsersByProprietor(proprietorId);
        return new ResponseEntity<>(managers, HttpStatus.OK);
    }

}