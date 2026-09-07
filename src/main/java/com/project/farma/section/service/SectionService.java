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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SectionService {
    private final FarmService farmService;
    private final SectionMapper sectionMapper;
    private final SectionRepository sectionRepository;

    @Transactional
    public SectionResponseDto createSection(SectionRequestDto requestDto) {
        Farm farm = farmService.getFarmById(requestDto.farmId());

        checkSectionUniquenessAndCapacity(requestDto);

        Section section = sectionMapper.toSectionEntity(requestDto);
        section.setAvailable(true);
        section.setFarm(farm);

        Section savedSection = sectionRepository.save(section);
        return sectionMapper.toSectionResponseDto(savedSection);
    }

    @Transactional
    public SectionResponseDto updateSection(Long sectionId, SectionRequestDto requestDto) {
        Section section = getSectionEntityById(sectionId);

        checkUpdateUniqueness(section, requestDto);

        section.setName(requestDto.name());
        section.setAnimalCategory(requestDto.animalCategory());
        section.setProductionType(requestDto.productionType());
        section.setCapacity(requestDto.capacity());

        Section updatedSection = sectionRepository.save(section);
        return sectionMapper.toSectionResponseDto(updatedSection);
    }

    @Transactional
    public void setSectionStatus(Long sectionId, boolean status) {
        Section section = getSectionEntityById(sectionId);
        section.setAvailable(status);
        sectionRepository.save(section);
    }

    public Page<SectionResponseDto> getSectionsByFarm(Long farmId, Pageable pageable) {
        return sectionRepository.findAllByFarmId(farmId, pageable)
                .map(sectionMapper::toSectionResponseDto);
    }

    public Page<SectionResponseDto> getAvailableSectionsByFarm(Long farmId, Pageable pageable) {
        return sectionRepository.findAllByFarmIdAndIsAvailableTrue(farmId, pageable)
                .map(sectionMapper::toSectionResponseDto);
    }

    public SectionResponseDto getSectionDetailsById(Long sectionId) {
        Section section = getSectionEntityById(sectionId);
        return sectionMapper.toSectionResponseDto(section);
    }


    // INTERNAL METHODS

    public Section getSectionEntityById(Long sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Section not found"));
    }

    // PRIVATE HELPER METHODS
    private void checkSectionUniquenessAndCapacity(SectionRequestDto requestDto) {
        if (sectionRepository.existsByNameAndFarmId(requestDto.name(), requestDto.farmId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A section with this name already exists on this farm");
        }
        if (requestDto.capacity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section Capacity must be greater than zero");
        }
    }

    private void checkUpdateUniqueness(Section currentSection, SectionRequestDto requestDto) {
        if (!currentSection.getName().equals(requestDto.name()) &&
                sectionRepository.existsByNameAndFarmId(requestDto.name(), requestDto.farmId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A section with this name already exists on this farm");
        }

        if (requestDto.capacity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section Capacity must be greater than zero");
        }
    }
}