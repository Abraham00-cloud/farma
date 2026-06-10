package com.project.farma.organisation.service;

import com.project.farma.organisation.dto.OrganisationRequestDto;
import com.project.farma.organisation.dto.OrganisationResponseDto;
import com.project.farma.organisation.mapper.OrganisationMapper;
import com.project.farma.organisation.model.Organisation;
import com.project.farma.organisation.repository.OrganisationRepository;
import com.project.farma.user.dto.UserRequestDto;
import com.project.farma.user.model.Role;
import com.project.farma.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrganisationService {
    private final OrganisationMapper organisationMapper;
    private final OrganisationRepository organisationRepository;
//    private final WalletService walletService;
    private final UserService userService;


    @Transactional
    public OrganisationResponseDto createOrganisation(OrganisationRequestDto requestDto) {
        validateOrganisationUniqueness(requestDto);

        Organisation organisation = organisationRepository.save(organisationMapper.toOrganisation(requestDto));

        createInitialProprietor(organisation, requestDto);
//        walletService.createWalletForOrganisation(organisation);


        return organisationMapper.toOrganisationResponseDto(organisation);

    }

    private void createInitialProprietor(Organisation organisation, OrganisationRequestDto requestDto) {
        UserRequestDto proprietorRequest = new UserRequestDto(
                requestDto.adminFirstName(),
                requestDto.adminLastName(),
                requestDto.email(),
                requestDto.password(),
                Role.PROPRIETOR,
                organisation.getId(),
                null
        );
        userService.createUser(proprietorRequest);

    }

    private void validateOrganisationUniqueness(OrganisationRequestDto requestDto) {
        if (organisationRepository.existsByRegistrationNumber(requestDto.registrationNumber())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The registration number is already in use");
        }

        if (organisationRepository.existsByEmail(requestDto.email())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email is already in use");
        }
    }

    public OrganisationResponseDto getOrganisationDetails(Long organisationId) {
        return organisationRepository.findById(organisationId)
                .map(organisationMapper::toOrganisationResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));
    }

    public Organisation findById(Long id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));
    }
}
