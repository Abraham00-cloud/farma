package com.project.farma.finance.service;

import com.project.farma.batch.model.Batch;
import com.project.farma.batch.service.BatchService;
import com.project.farma.finance.calculator.FinancialPnlCalculator;
import com.project.farma.finance.dto.BatchFinancialPnlResponseDto;
import com.project.farma.finance.dto.BatchFinancialSummaryDto;
import com.project.farma.finance.dto.FarmFinancialOverviewDto;
import com.project.farma.finance.dto.FinancialCategoryBreakdownDto;
import com.project.farma.finance.mapper.FinancialPnlMapper;
import com.project.farma.transaction.model.Transaction;
import com.project.farma.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialPnlService {
    private final BatchService batchService;
    private final TransactionService transactionService;
    private final FinancialPnlCalculator calculator;
    private final FinancialPnlMapper mapper;

    public BatchFinancialPnlResponseDto getBatchPnlAnalysis(Long batchId) {
        Batch batch = batchService.getBatchById(batchId);
        List<Transaction> transactions = transactionService.getRawTransactionsForBatch(batchId);


        double revenue = calculator.calculateTotalRevenue(transactions);
        double expenses = calculator.calculateTotalExpenses(transactions);
        double netProfit = calculator.calculateNetProfit(revenue, expenses);
        double margin = calculator.calculateProfitMarginPercentage(netProfit, revenue);


        int population = batch.getCurrentCount() > 0 ? batch.getCurrentCount() : batch.getInitialCount();
        double costPerBird = calculator.calculateUnitCost(expenses, population);
        double revenuePerBird = calculator.calculateUnitCost(revenue, population);
        double profitPerBird = calculator.calculateUnitCost(netProfit, population);


        List<FinancialCategoryBreakdownDto> expenseChart = calculator.generateExpenseCategoryBreakdown(transactions, expenses);

        return mapper.toPnlResponse(
                batch, revenue, expenses, netProfit, margin,
                costPerBird, revenuePerBird, profitPerBird, expenseChart
        );
    }

    public FarmFinancialOverviewDto getFarmFinancialOverview(Long farmId) {
        List<Batch> farmBatches = batchService.getBatchesByFarmId(farmId);
        List<Transaction> farmTransactions = transactionService.getRawTransactionsForFarm(farmId);

        double totalRevenue = calculator.calculateTotalRevenue(farmTransactions);
        double totalExpenses = calculator.calculateTotalExpenses(farmTransactions);
        double netProfit = calculator.calculateNetProfit(totalRevenue, totalExpenses);
        double margin = calculator.calculateProfitMarginPercentage(netProfit, totalRevenue);

        List<BatchFinancialSummaryDto> batchSummaries = farmBatches.stream().map(batch -> {
            List<Transaction> batchTxns = farmTransactions.stream()
                    .filter(t -> t.getBatch() != null && t.getBatch().getId().equals(batch.getId()))
                    .toList();

            double bRev = calculator.calculateTotalRevenue(batchTxns);
            double bExp = calculator.calculateTotalExpenses(batchTxns);
            int pop = batch.getCurrentCount() > 0 ? batch.getCurrentCount() : batch.getInitialCount();

            return calculator.summarizeBatch(
                    batch.getId(),
                    batch.getBatchNumber(),
                    batch.getSection().getName(),
                    batch.getBreed() != null ? batch.getBreed().name() : "N/A",
                    batch.getStatus().name(),
                    bRev,
                    bExp,
                    pop
            );
        }).toList();

        List<FinancialCategoryBreakdownDto> expenseChart = calculator.generateExpenseCategoryBreakdown(farmTransactions, totalExpenses);

        int activeCount = (int) farmBatches.stream().filter(b -> "ACTIVE".equals(b.getStatus().name())).count();
        int completedCount = (int) farmBatches.stream().filter(b -> "COMPLETED".equals(b.getStatus().name())).count();

        String farmName = farmBatches.isEmpty() ? "Farm #" + farmId : farmBatches.get(0).getSection().getFarm().getName();

        return new FarmFinancialOverviewDto(
                farmId,
                farmName,
                totalRevenue,
                totalExpenses,
                netProfit,
                margin,
                farmBatches.size(),
                activeCount,
                completedCount,
                batchSummaries,
                expenseChart
        );
    }
}
