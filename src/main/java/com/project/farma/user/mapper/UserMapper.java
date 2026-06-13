package com.project.farma.user.mapper;


import com.project.farma.user.dto.UserRequestDto;
import com.project.farma.user.dto.UserResponseDto;
import com.project.farma.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public User toUserEntity(UserRequestDto requestDto) {
        String encodedPassword = passwordEncoder.encode(requestDto.password());
        return User.builder()
                .firstName(requestDto.firstName())
                .lastName(requestDto.lastName())
                .email(requestDto.email())
                .password(encodedPassword)
                .role(requestDto.role())
                .build();
    }

    public UserResponseDto toUserResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getOrganisation() != null ? user.getOrganisation().getId() : null,
                user.getParent() != null ? user.getParent().getId() : null,
                user.getCreatedAt()
        );
    }
}
