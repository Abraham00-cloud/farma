package com.project.farma.organisation.mapper;

import com.project.farma.organisation.dto.OrganisationRequestDto;
import com.project.farma.organisation.dto.OrganisationResponseDto;
import com.project.farma.organisation.model.Organisation;
import lombok.Builder;
import org.springframework.stereotype.Service;

@Service

public class OrganisationMapper {
    public Organisation toOrganisation(OrganisationRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        return Organisation.builder()
                .name(requestDto.name())
                .email(requestDto.email())
                .registrationNumber(requestDto.registrationNumber())
                .organisationType(requestDto.organisationType())
                .build();
    }

    public OrganisationResponseDto toOrganisationResponseDto(Organisation organisation) {
        return new OrganisationResponseDto(
                organisation.getId(),
                organisation.getName(),
                organisation.getRegistrationNumber(),
                organisation.getOrganisationType(),
                organisation.getCreatedAt()
        );
    }
}
