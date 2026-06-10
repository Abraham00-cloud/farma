package com.project.farma.transaction.dto;

import com.project.farma.organisation.model.Organisation;
import com.project.farma.transaction.model.TransactionCategory;
import com.project.farma.transaction.model.TransactionType;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record TransactionRequestDto(
        @NotNull(message = " Amount is required")
        @Min(value = 0, message = "Amount cannot be less than zero")
        Double amount,

        @NotNull(message = "Transaction type is required")
        TransactionType transactionType,

        @NotNull(message = "Transaction category is required")
        TransactionCategory transactionCategory,

        @NotNull(message = "Transaction description is required")
        String description,

        @NotNull(message = "Transaction date is required")
        @PastOrPresent(message = "Transaction date cannot be in the future")
        LocalDate transactionDate,


        boolean isCashFlow,

        @NotNull(message = "Organisation ID is required")
        Long organisationId,

        @NotNull(message = "BatchId is required")
        Long batchId
) {
}
