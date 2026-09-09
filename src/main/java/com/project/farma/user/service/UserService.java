package com.project.farma.user.service;
import com.project.farma.common.event.dto.ManagerCreatedEvent;
import com.project.farma.common.event.service.EmailValidator;
import com.project.farma.organisation.model.Organisation;
import com.project.farma.organisation.repository.OrganisationRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final OrganisationRepository organisationRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDto createUser(UserRequestDto requestDto, Long currentPrincipalId) {
        EmailValidator.validateOriginalEmail(requestDto.email());

        if (userRepository.existsByEmail(requestDto.email())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        Organisation organisation = organisationRepository.findById(requestDto.organisationId())
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        User user = userMapper.toUserEntity(requestDto);
        user.setOrganisation(organisation);

        if (requestDto.role() == Role.MANAGER) {
            handleManagerCreation(user, currentPrincipalId, requestDto.organisationId());
        } else if (requestDto.role() == Role.PROPRIETOR) {
            handleProprietorCreation(user);
        }

        User savedUser = userRepository.save(user);

        handlePostCreationEvents(savedUser, requestDto.password());

        return userMapper.toUserResponseDto(savedUser);
    }

    public AuthResponseDto authenticate(LoginRequestDto loginRequestDto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.email(), loginRequestDto.password())
        );
        User user = userRepository.findByEmail(loginRequestDto.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Long organisationId = user.getOrganisation() != null ? user.getOrganisation().getId() : null;
        String token = jwtService.generateToken(loginRequestDto.email(), user.getId(), organisationId);

        return new AuthResponseDto(token, user.getEmail(), user.getRole(), organisationId, user.isRequiresPasswordChange());
    }

    @Transactional
    public void deactivateUser(Long userIdToDeactivate, Long currentPrincipalId) {
        User userToDeactivate = findById(userIdToDeactivate);
        User currentUser = findById(currentPrincipalId);

        handleDeactivationValidation(userToDeactivate, currentUser);

        userToDeactivate.setActive(false);
        if (userToDeactivate.getManagedFarms() != null) {
            userToDeactivate.getManagedFarms().forEach(farm -> farm.setManager(null));
        }

        userRepository.save(userToDeactivate);
    }

    public Page<UserResponseDto> getUsersByProprietor(Long proprietorID, Long currentPrincipalId, Pageable pageable) {
        if (!proprietorID.equals(currentPrincipalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view managers belonging to another Proprietor.");
        }

        return userRepository.findAllByParentId(proprietorID, pageable)
                .map(userMapper::toUserResponseDto);
    }

    public UserResponseDto getUserById(Long requestedUserId, Long currentPrincipalOrgId) {
        User user = findById(requestedUserId);

        if (!user.getOrganisation().getId().equals(currentPrincipalOrgId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: User belongs to a different organisation.");
        }

        return userMapper.toUserResponseDto(user);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional
    public void forceUpdatePassword(Long userId, String newPassword) {
        User user = findById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRequiresPasswordChange(false); // Clear the security flag
        userRepository.save(user);
    }

    // PRIVATE HELPER METHODS

    private void handleProprietorCreation(User user) {
        user.setParent(null);
    }

    private void handleManagerCreation(User user, Long currentPrincipalId, Long orgId) {
        if (currentPrincipalId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You must be logged in to create a manager.");
        }

        User proprietor = findById(currentPrincipalId);

        if (!proprietor.getOrganisation().getId().equals(orgId)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot assign managers to an organisation you do not own.");
        }

        user.setParent(proprietor);
        // 4. Force managers to update their auto-generated password on first login
        user.setRequiresPasswordChange(true);
    }

    private void handlePostCreationEvents(User savedUser, String rawPassword) {
        if (savedUser.getRole() == Role.MANAGER) {
            ManagerCreatedEvent event = new ManagerCreatedEvent(
                    savedUser.getFirstName(),
                    savedUser.getEmail(),
                    rawPassword,
                    savedUser.getOrganisation().getName()
            );
            applicationEventPublisher.publishEvent(event);
        }
    }

    private void handleDeactivationValidation(User userToDeactivate, User currentUser) {
        if (userToDeactivate.getRole() == Role.PROPRIETOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Proprietors cannot delete their account without first transferring ownership.");
        }

        if (!userToDeactivate.getOrganisation().getId().equals(currentUser.getOrganisation().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot deactivate users outside your organisation.");
        }
    }
}