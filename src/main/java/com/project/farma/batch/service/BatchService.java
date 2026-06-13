package com.project.farma.batch.service;

import com.project.farma.batch.dto.BatchRequestDto;
import com.project.farma.batch.dto.BatchResponseDto;
import com.project.farma.batch.mapper.BatchMapper;
import com.project.farma.batch.model.Batch;
import com.project.farma.batch.model.Status;
import com.project.farma.batch.repository.BatchRepository;
import com.project.farma.dailyLogs.dto.DailyLogRequestDto;
import com.project.farma.section.model.Section;
import com.project.farma.section.service.SectionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BatchService {
    private final SectionService sectionService;
    private final BatchRepository batchRepository;
    private final BatchMapper batchMapper;

    @Transactional
    public BatchResponseDto createBatch(BatchRequestDto requestDto) {
        Section section = sectionService.getSectionEntityById(requestDto.sectionId());

        handleBatchValidation(section, requestDto);

        Batch batch = batchMapper.toBatchEntity(requestDto);
        batch.setStatus(Status.ACTIVE);
        batch.setCurrentCount(requestDto.initialCount());
        batch.setMortalityCount(0);
        batch.setExpectedEndDate(requestDto.expectedEndDate());

        String generateBatchNumber = generateBatchNumber(section);
        batch.setBatchNumber(generateBatchNumber);

        sectionService.setSectionStatus(section.getId(), false);

        Batch savedBatch = batchRepository.save(batch);

        return batchMapper.toBatchResponseDto(savedBatch);

    }

    private String generateBatchNumber(Section section) {
        return section.getName().toUpperCase().replace(" ", "") + "-" + System.currentTimeMillis();
    }

    private void handleBatchValidation(Section section,BatchRequestDto requestDto) {
        if (!section.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Farm section is currently not available");
        }

        if (requestDto.initialCount() > section.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Batch size exceeds the section capacity of " + section.getCapacity());
        }

    }

    public Batch getBatchById(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found"));
    }

    public BatchResponseDto getBatchDetailsById(Long id) {
        return batchRepository.findById(id)
                .map(batchMapper::toBatchResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found"));
    }

    public void updateBatchMortality(Long batchId, Integer deathCount) {
        if (deathCount == null || deathCount == 0) return;
        Batch batch = getBatchById(batchId);
        Integer totalDeaths = batch.getMortalityCount() + deathCount;
        batch.setMortalityCount(totalDeaths);

        Integer newPopulation = batch.getCurrentCount() - deathCount;
        batch.setCurrentCount(newPopulation);

        batchRepository.save(batch);

    }
}
