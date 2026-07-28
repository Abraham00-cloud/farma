package com.project.farma.batch.mapper;

import com.project.farma.batch.dto.BatchRequestDto;
import com.project.farma.batch.dto.BatchResponseDto;
import com.project.farma.batch.model.Batch;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BatchMapper {
    public Batch toBatchEntity(BatchRequestDto dto) {
        return Batch.builder()
                .batchNumber(dto.batchNumber())
                .initialCount(dto.initialCount())
                .currentCount(dto.initialCount())
                .mortalityCount(0)
                .startDate(dto.startDate())
                .expectedEndDate(dto.expectedEndDate())
                .breed(dto.breed())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public BatchResponseDto toBatchResponseDto(Batch batch) {
        return new BatchResponseDto(
                batch.getId(),
                batch.getBatchNumber(),
                batch.getSection().getName(),
                batch.getInitialCount(),
                batch.getCurrentCount(),
                batch.getMortalityCount(),
                batch.getSection().getAnimalCategory(),
                batch.getSection().getProductionType(),
                batch.getStatus(),
                batch.getStartDate(),
                batch.getExpectedEndDate(),
                batch.getActualEndDate(),
                batch.getCreatedAt()


        );
    }
}
