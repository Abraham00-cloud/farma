package com.project.farma.transaction.dto;

import com.project.farma.transaction.model.TransactionCategory;
import com.project.farma.transaction.model.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InternalTransactionRequestDto(
        @NotNull(message = "Organisation ID is required")
        Long organisationId,

        @NotNull(message = "Batch ID is required")
        Long batchId,

        @NotNull(message = "Amount is required")
        @Min(value = 0, message = "Amount cannot be less than zero")
        Double amount,

        @NotNull(message = "Transaction type is required")
        TransactionType type,

        @NotNull(message = "Transaction category is required")
        TransactionCategory category,

        @NotNull(message = "Transaction description is required")
        String description
) {}