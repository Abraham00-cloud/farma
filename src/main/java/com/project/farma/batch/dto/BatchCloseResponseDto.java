package com.project.farma.batch.dto;


import com.project.farma.batch.model.Status;
import java.time.LocalDate;

public record BatchCloseResponseDto(
        Long batchId,
        String batchNumber,
        Status status,
        LocalDate startDate,
        LocalDate actualEndDate,
        Integer finalBirdCount,
        Integer totalBirdsSold,
        Double totalSaleRevenue,
        Boolean sectionUnlocked,
        String harvestNotes
) {}
