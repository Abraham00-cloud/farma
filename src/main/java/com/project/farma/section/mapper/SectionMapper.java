package com.project.farma.section.mapper;

import com.project.farma.section.dto.SectionRequestDto;
import com.project.farma.section.dto.SectionResponseDto;
import com.project.farma.section.model.Section;
import org.springframework.stereotype.Component;

@Component
public class SectionMapper {
    public Section toSectionEntity(SectionRequestDto requestDto) {
        return Section.builder()
                .name(requestDto.name())
                .animalCategory(requestDto.animalCategory())
                .productionType(requestDto.productionType())
                .capacity(requestDto.capacity())
                .build();
    }

    public SectionResponseDto toSectionResponseDto(Section section) {
        return new SectionResponseDto(
                section.getId(),
                section.getName(),
                section.getFarm().getId(),
                section.getAnimalCategory(),
                section.getProductionType(),
                section.getCapacity()
        );
    }
}
