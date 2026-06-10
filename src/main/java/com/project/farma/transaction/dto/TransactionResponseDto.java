package com.project.farma.transaction.dto;

import com.project.farma.transaction.model.TransactionCategory;
import com.project.farma.transaction.model.TransactionType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponseDto(
        Long transactionId,
        Double amount,
        TransactionType type,
        TransactionCategory category,
        LocalDate transactionDate,
        String description,
        boolean isCashFlow,
        Long batchId,
        String batchNumber,
        LocalDateTime createdAt

) {


}
