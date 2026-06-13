package com.project.farma.section.service;

import com.project.farma.farm.model.Farm;
import com.project.farma.farm.service.FarmService;
import com.project.farma.section.dto.SectionRequestDto;
import com.project.farma.section.dto.SectionResponseDto;
import com.project.farma.section.mapper.SectionMapper;
import com.project.farma.section.model.Section;
import com.project.farma.section.repository.SectionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final FarmService farmService;
    private final SectionMapper sectionMapper;
    private final SectionRepository sectionRepository;

    @Transactional
    public SectionResponseDto createSection(SectionRequestDto requestDto) {
        Farm farm = farmService.getFarmById(requestDto.farmId());
        handleSectionValidation(requestDto);

        Section section = sectionMapper.toSectionEntity(requestDto);
        section.setAvailable(true);
        section.setFarm(farm);

        Section savedSection = sectionRepository.save(section);
        return sectionMapper.toSectionResponseDto(savedSection);
    }

    @Transactional
    public void setSectionStatus(Long sectionId, boolean status) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));

        section.setAvailable(status);
        sectionRepository.save(section);
    }

    public List<SectionResponseDto> getSectionsByFarm(Long farmId) {
        return sectionRepository.findAllByFarmId(farmId)
                .stream()
                .map(sectionMapper::toSectionResponseDto)
                .toList();
    }

    public List<SectionResponseDto> getAvailableSectionsByFarm(Long farmId) {
        return sectionRepository.findAllByFarmIdAndIsAvailableTrue(farmId)
                .stream()
                .map(sectionMapper::toSectionResponseDto)
                .toList();
    }

    public Section getSectionEntityById(Long sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));
    }

    public SectionResponseDto getSectionDetailsById(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));
        return sectionMapper.toSectionResponseDto(section);
    }

    @Transactional
    public SectionResponseDto updateSection(Long sectionId, SectionRequestDto requestDto) {
        Section section = getSectionEntityById(sectionId);
        if (!section.getName().equals(requestDto.name()) &&
                sectionRepository.existsByNameAndFarmId(requestDto.name(), requestDto.farmId())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Name already esists on this farm");
        }

        section.setName(requestDto.name());
        section.setAnimalCategory(requestDto.animalCategory());
        section.setProductionType(requestDto.productionType());
        section.setCapacity(requestDto.capacity());

        return sectionMapper.toSectionResponseDto(sectionRepository.save(section));
    }

    private void handleSectionValidation(SectionRequestDto requestDto) {
        if (sectionRepository.existsByNameAndFarmId(requestDto.name(), requestDto.farmId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A section with this name already exists on this farm");
        }
        if (requestDto.capacity()<= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section Capacity must be greater than zero");
        }
    }
}
