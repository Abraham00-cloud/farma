package com.project.farma.batch.mapper;

import com.project.farma.batch.dto.BatchRequestDto;
import com.project.farma.batch.dto.BatchResponseDto;
import com.project.farma.batch.model.Batch;
import org.springframework.stereotype.Component;

@Component
public class BatchMapper {
    public Batch toBatchEntity(BatchRequestDto requestDto) {
        return Batch.builder()
                .batchNumber(requestDto.batchNumber())
                .initialCount(requestDto.initialCount())
                .currentCount(requestDto.initialCount())
                .startDate(requestDto.startDate())
                .expectedEndDate(requestDto.expectedEndDate())
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
