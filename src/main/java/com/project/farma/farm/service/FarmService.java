package com.project.farma.farm.service;

import com.project.farma.farm.dto.FarmRequestDto;
import com.project.farma.farm.dto.FarmResponseDto;
import com.project.farma.farm.mapper.FarmMapper;
import com.project.farma.farm.model.Farm;
import com.project.farma.farm.repository.FarmRepository;
import com.project.farma.organisation.model.Organisation;
import com.project.farma.organisation.service.OrganisationService;
import com.project.farma.user.model.Role;
import com.project.farma.user.model.User;
import com.project.farma.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmService {
    private final FarmMapper farmMapper;
    private final OrganisationService organisationService;
    private final UserService userService;
    private final FarmRepository farmRepository;

    @Transactional
    public FarmResponseDto createFarm(FarmRequestDto requestDto) {
        Organisation organisation = organisationService.findById(requestDto.organisationId());

        User manager = userService.findById(requestDto.managerId());
        handleOrganisationAndManagerValidation(organisation, manager, requestDto);

        Farm farm = farmMapper.toFarmEntity(requestDto);

        farm.setOrganisation(organisation);
        farm.setManager(manager);

        Farm savedFarm = farmRepository.save(farm);
        return farmMapper.toFarmResponseDto(savedFarm);
    }

    public List<FarmResponseDto> getFarmsByOrganisation(Long organisationId) {
        return farmRepository.findAllByOrganisationId(organisationId)
                .stream()
                .map(farmMapper::toFarmResponseDto)
                .toList();
    }

    public Farm getFarmById(Long farmId) {
        return farmRepository.findById(farmId)
                .orElseThrow(() -> new EntityNotFoundException("Farm not found"));
    }

    public FarmResponseDto getFarmDetailsById(Long farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new EntityNotFoundException("Farm not found"));
        return farmMapper.toFarmResponseDto(farm);
    }


    private void handleOrganisationAndManagerValidation(Organisation organisation, User manager, FarmRequestDto requestDto) {
        if (farmRepository.existsByNameAndOrganisationId(requestDto.name(), requestDto.organisationId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A farm with this name already exists in your organisation");
        }
        if (manager.getRole() != Role.MANAGER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must be a Manager");
        }

        if (!manager.getOrganisation().getId().equals(requestDto.organisationId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Manager/Organisation mismatch");
        }
    }
}

