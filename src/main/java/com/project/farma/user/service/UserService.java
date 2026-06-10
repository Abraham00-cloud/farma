package com.project.farma.user.service;

import com.project.farma.organisation.model.Organisation;
import com.project.farma.organisation.repository.OrganisationRepository;
import com.project.farma.organisation.service.OrganisationService;
import com.project.farma.security.JwtService;
import com.project.farma.user.dto.AuthResponseDto;
import com.project.farma.user.dto.LoginRequestDto;
import com.project.farma.user.dto.UserRequestDto;
import com.project.farma.user.dto.UserResponseDto;
import com.project.farma.user.mapper.UserMapper;
import com.project.farma.user.model.Role;
import com.project.farma.user.model.User;
import com.project.farma.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final OrganisationRepository organisationRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public UserResponseDto createUser(UserRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.email())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        Organisation organisation = organisationRepository.findById(requestDto.organisationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organisation not found"));

        if (organisation == null) {
            throw new EntityNotFoundException("Organisation not found");
        }

        User user = userMapper.toUserEntity(requestDto);
        user.setOrganisation(organisation);

        if (requestDto.role() == Role.MANAGER) {
            handleManagerCreation(user, requestDto);
        } else if (requestDto.role() == Role.PROPRIETOR) {
            handleProprietorCreation(user);
        }

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(savedUser);
    }

    public AuthResponseDto authenticate(LoginRequestDto loginRequestDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.email(), loginRequestDto.password())
        );
        User user = userRepository.findByEmail(loginRequestDto.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Long organisationId = user.getOrganisation() != null ? user.getOrganisation().getId() : null;
        String token = jwtService.generateToken(loginRequestDto.email(), user.getId(), organisationId);
        return new AuthResponseDto(token, user.getEmail(), user.getRole(), organisationId);
    }

    private void handleProprietorCreation(User user) {
        user.setParent(null);
    }

    private void handleManagerCreation(User user, UserRequestDto requestDto) {
        if(requestDto.parentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A manager must be attached to a Proprietor");
        }

        User proprietor = userRepository.findById(requestDto.parentId())
                .orElseThrow(() -> new EntityNotFoundException("Proprietor not found"));

        if (!proprietor.getOrganisation().getId().equals(user.getOrganisation().getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cross-organisation assignment is not allowed");
        }

        user.setParent(proprietor);
    }

    public List<UserResponseDto> getUsersByProprietor(Long proprietorID) {
        return userRepository.findAllByParentId(proprietorID)
                .stream()
                .map(userMapper::toUserResponseDto)
                .toList();
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
