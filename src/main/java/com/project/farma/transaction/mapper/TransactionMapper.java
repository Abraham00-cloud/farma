package com.project.farma.transaction.mapper;

import com.project.farma.transaction.dto.TransactionRequestDto;
import com.project.farma.transaction.dto.TransactionResponseDto;
import com.project.farma.transaction.model.Transaction;
import com.project.farma.transaction.model.TransactionCategory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransactionMapper {
    public Transaction toTransactionEntity(TransactionRequestDto transactionRequestDto) {
        return Transaction.builder()
                .amount(transactionRequestDto.amount())
                .type(transactionRequestDto.transactionType())
                .category(transactionRequestDto.transactionCategory())
                .transactionDate(transactionRequestDto.transactionDate())
                .description(transactionRequestDto.description())
                .isCashFlow(determineIfCashFlow(transactionRequestDto.transactionCategory()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public TransactionResponseDto toTransactionResponseDto(Transaction transaction) {
        return new TransactionResponseDto(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getCategory(),
                transaction.getTransactionDate(),
                transaction.getDescription(),
                transaction.isCashFlow(),
                transaction.getBatch() != null ? transaction.getId() : null,
                transaction.getBatch() != null ? transaction.getBatch().getBatchNumber() : null,
                transaction.getCreatedAt()
        );
    }

    private boolean determineIfCashFlow(TransactionCategory category) {
        return switch (category) {
            case FEED_CONSUMPTION, MEDICINE_CONSUMPTION, VACCINE_CONSUMPTION -> false;
            default -> true;
        };
    }
}
