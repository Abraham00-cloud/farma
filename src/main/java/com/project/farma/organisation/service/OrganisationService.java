package com.project.farma.organisation.service;

import com.project.farma.common.event.dto.OrganisationRegisteredEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrganisationService {
    private final OrganisationMapper organisationMapper;
    private final OrganisationRepository organisationRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrganisationResponseDto createOrganisation(OrganisationRequestDto requestDto) {
        checkOrganisationUniqueness(requestDto);

        Organisation organisation = organisationRepository.save(organisationMapper.toOrganisation(requestDto));

        createInitialProprietor(organisation, requestDto);
        sendOrganisationRegisteredEvent(requestDto, organisation.getName());

        return organisationMapper.toOrganisationResponseDto(organisation);
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

    // PRIVATE HELPER METHODS


    private void checkOrganisationUniqueness(OrganisationRequestDto requestDto) {
        if (organisationRepository.existsByRegistrationNumber(requestDto.registrationNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The registration number is already in use");
        }

        if (organisationRepository.existsByEmail(requestDto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }
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

        userService.createUser(proprietorRequest, null);
    }

    private void sendOrganisationRegisteredEvent(OrganisationRequestDto requestDto, String orgName) {
        eventPublisher.publishEvent(new OrganisationRegisteredEvent(
                requestDto.email(),
                requestDto.adminFirstName(),
                orgName,
                requestDto.registrationNumber(),
                requestDto.organisationType()
        ));
    }
}