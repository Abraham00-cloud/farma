package com.project.farma.finance.calculator;

import com.project.farma.finance.dto.BatchFinancialSummaryDto;
import com.project.farma.finance.dto.FinancialCategoryBreakdownDto;
import com.project.farma.transaction.model.Transaction;
import com.project.farma.transaction.model.TransactionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FinancialPnlCalculator {
    public double calculateTotalRevenue(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.CREDIT)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double calculateTotalExpenses(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.DEBIT)
                .filter(this::isPnlExpense)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private boolean isPnlExpense(Transaction t) {
        return switch (t.getCategory()) {
            case FEED_PURCHASE, MEDICINE_PURCHASE, VACCINE_PURCHASE, EQUIPMENT_PURCHASE -> false;
            default -> true;
        };
    }

    public double calculateNetProfit(double revenue, double expenses) {
        return roundToTwoDecimals(revenue - expenses);
    }

    public double calculateProfitMarginPercentage(double netProfit, double revenue) {
        if (revenue == 0.0) return 0.0;
        return roundToTwoDecimals((netProfit / revenue) * 100.0);
    }

    public double calculateUnitCost(double totalAmount, int birdCount) {
        if (birdCount == 0) return 0.0;
        return roundToTwoDecimals(totalAmount / birdCount);
    }

    public List<FinancialCategoryBreakdownDto> generateExpenseCategoryBreakdown(List<Transaction> transactions, double totalExpenses) {
        if (totalExpenses == 0.0) return List.of();

        Map<String, Double> expensesByCategory = transactions.stream()
                .filter(t -> t.getType() == TransactionType.DEBIT)
                .filter(this::isPnlExpense)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().name(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        return expensesByCategory.entrySet().stream()
                .map(entry -> {
                    double categoryTotal = entry.getValue();
                    double percentage = (categoryTotal / totalExpenses) * 100.0;
                    return new FinancialCategoryBreakdownDto(
                            entry.getKey(),
                            roundToTwoDecimals(categoryTotal),
                            roundToTwoDecimals(percentage)
                    );
                })
                .collect(Collectors.toList());
    }

    public BatchFinancialSummaryDto summarizeBatch(
            Long batchId,
            String batchNumber,
            String sectionName,
            String breed,
            String status,
            double revenue,
            double expenses,
            int population
    ) {
        double netProfit = calculateNetProfit(revenue, expenses);
        double margin = calculateProfitMarginPercentage(netProfit, revenue);
        double costPerBird = calculateUnitCost(expenses, population);
        double profitPerBird = calculateUnitCost(netProfit, population);

        return new BatchFinancialSummaryDto(
                batchId,
                batchNumber,
                sectionName,
                breed,
                status,
                roundToTwoDecimals(revenue),
                roundToTwoDecimals(expenses),
                netProfit,
                margin,
                costPerBird,
                profitPerBird
        );
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}