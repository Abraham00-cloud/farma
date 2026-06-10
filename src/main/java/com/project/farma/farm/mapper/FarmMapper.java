package com.project.farma.farm.mapper;


import com.project.farma.farm.dto.FarmRequestDto;
import com.project.farma.farm.dto.FarmResponseDto;
import com.project.farma.farm.model.Farm;
import org.springframework.stereotype.Component;

@Component
public class FarmMapper {
    public Farm toFarmEntity(FarmRequestDto requestDto) {
        return Farm.builder()
                .name(requestDto.name())
                .address(requestDto.address())
                .build();
    }

    public FarmResponseDto toFarmResponseDto(Farm farm) {
        return new FarmResponseDto(
                farm.getId(),
                farm.getName(),
                farm.getAddress(),
                farm.getOrganisation().getId(),
                farm.getManager().getLastName() + " "  + farm.getManager().getFirstName(),
                farm.getCreatedAt()
        );
    }

}
