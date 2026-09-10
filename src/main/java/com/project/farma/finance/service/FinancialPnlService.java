package com.project.farma.finance.service;

import com.project.farma.batch.model.Batch;
import com.project.farma.batch.service.BatchService;
import com.project.farma.dailyLogs.model.DailyLog;
import com.project.farma.dailyLogs.service.DailyLogService;
import com.project.farma.farm.model.Farm;
import com.project.farma.farm.service.FarmService;
import com.project.farma.finance.calculator.FinancialPnlCalculator;
import com.project.farma.finance.dto.*;
import com.project.farma.finance.mapper.FinancialPnlMapper;
import com.project.farma.inventory.model.Inventory;
import com.project.farma.inventory.service.InventoryService;
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

    // Injected for the Valuation Engine
    private final DailyLogService dailyLogService;
    private final InventoryService inventoryService;
    private final FarmService farmService;

    public BatchFinancialPnlResponseDto getBatchPnlAnalysis(Long batchId) {
        Batch batch = batchService.getBatchById(batchId);
        List<Transaction> transactions = transactionService.getRawTransactionsForBatch(batchId);

        double revenue = calculator.calculateTotalRevenue(transactions);
        double expenses = calculator.calculateTotalExpenses(transactions);
        double netProfit = calculator.calculateNetProfit(revenue, expenses);
        double margin = calculator.calculateProfitMarginPercentage(netProfit, revenue);

        int population = determinePopulationForMath(batch);
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
        List<Batch> farmBatches = batchService.getBatchEntitiesByFarmId(farmId);
        List<Transaction> farmTransactions = transactionService.getRawTransactionsForFarm(farmId);

        double totalRevenue = calculator.calculateTotalRevenue(farmTransactions);
        double totalExpenses = calculator.calculateTotalExpenses(farmTransactions);
        double netProfit = calculator.calculateNetProfit(totalRevenue, totalExpenses);
        double margin = calculator.calculateProfitMarginPercentage(netProfit, totalRevenue);

        List<BatchFinancialSummaryDto> batchSummaries = farmBatches.stream()
                .map(batch -> buildBatchSummary(batch, farmTransactions))
                .toList();

        List<FinancialCategoryBreakdownDto> expenseChart = calculator.generateExpenseCategoryBreakdown(farmTransactions, totalExpenses);

        int activeCount = (int) farmBatches.stream().filter(b -> "ACTIVE".equals(b.getStatus().name())).count();
        int completedCount = (int) farmBatches.stream().filter(b -> "COMPLETED".equals(b.getStatus().name())).count();
        String farmName = farmBatches.isEmpty() ? "Farm #" + farmId : farmBatches.get(0).getSection().getFarm().getName();

        return new FarmFinancialOverviewDto(
                farmId, farmName, totalRevenue, totalExpenses, netProfit, margin,
                farmBatches.size(), activeCount, completedCount, batchSummaries, expenseChart
        );
    }

    // =========================================================================================
    // NEW: VALUATION / SCENARIO PLANNER ENGINE
    // =========================================================================================

    public ValuationResponseDto calculateProjectedValuation(ValuationRequestDto request) {
        String scopeName = "";
        int liveBirds = 0;
        double totalWeightKg = 0.0;
        double produceUnits = 0.0;

        // Split historical ledger into actuals
        double actualSunkCosts = 0.0;
        double realizedRevenue = 0.0;

        double pricePerKg = request.projectedPricePerKg() != null ? request.projectedPricePerKg() : 0.0;
        double pricePerProduce = request.projectedPricePerProduceUnit() != null ? request.projectedPricePerProduceUnit() : 0.0;

        switch (request.scope().toUpperCase()) {
            case "BATCH":
                Batch batch = batchService.getBatchById(request.scopeId());
                scopeName = "Batch #" + batch.getBatchNumber();
                liveBirds = batch.getCurrentCount();
                totalWeightKg = calculateBatchWeight(batch);

                // Fetch true financial history for this batch
                List<Transaction> batchTxns = transactionService.getRawTransactionsForBatch(batch.getId());
                actualSunkCosts = calculator.calculateTotalExpenses(batchTxns);
                realizedRevenue = calculator.calculateTotalRevenue(batchTxns);
                break;

            case "FARM":
                Farm farm = farmService.getFarmById(request.scopeId());
                scopeName = "Farm: " + farm.getName();

                List<Batch> farmBatches = batchService.getBatchEntitiesByFarmId(farm.getId());
                for (Batch b : farmBatches) {
                    if ("ACTIVE".equals(b.getStatus().name())) {
                        liveBirds += b.getCurrentCount();
                        totalWeightKg += calculateBatchWeight(b);
                    }
                }

                produceUnits = getFarmProduceStock(farm);

                // Fetch true financial history for the entire farm
                List<Transaction> farmTxns = transactionService.getRawTransactionsForFarm(farm.getId());
                actualSunkCosts = calculator.calculateTotalExpenses(farmTxns);
                realizedRevenue = calculator.calculateTotalRevenue(farmTxns);
                break;

            case "ORGANISATION":
                scopeName = "Entire Organisation ID: " + request.scopeId();
                List<Farm> allFarms = farmService.getFarmsByOrganisationId(request.scopeId());

                for (Farm f : allFarms) {
                    List<Batch> fb = batchService.getBatchEntitiesByFarmId(f.getId());
                    for (Batch b : fb) {
                        if ("ACTIVE".equals(b.getStatus().name())) {
                            liveBirds += b.getCurrentCount();
                            totalWeightKg += calculateBatchWeight(b);
                        }
                    }
                    produceUnits += getFarmProduceStock(f);

                    // Fetch true financial history across all farms
                    List<Transaction> orgFarmTxns = transactionService.getRawTransactionsForFarm(f.getId());
                    actualSunkCosts += calculator.calculateTotalExpenses(orgFarmTxns);
                    realizedRevenue += calculator.calculateTotalRevenue(orgFarmTxns);
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid scope. Use BATCH, FARM, or ORGANISATION");
        }

        // 1. Calculate Future Asset Value (What you are predicting to sell)
        double projectedMeatRevenue = totalWeightKg * pricePerKg;
        double projectedProduceRevenue = produceUnits * pricePerProduce;
        double totalUnsoldAssetValue = projectedMeatRevenue + projectedProduceRevenue;

        // 2. The Final Valuation Math (Realized + Unsold - Sunk Costs)
        double totalProjectedRevenue = realizedRevenue + totalUnsoldAssetValue;
        double projectedNetProfit = totalProjectedRevenue - actualSunkCosts;
        double profitMargin = totalProjectedRevenue > 0 ? (projectedNetProfit / totalProjectedRevenue) * 100 : 0.0;

        return new ValuationResponseDto(
                request.scope().toUpperCase(),
                scopeName,
                liveBirds,
                totalWeightKg,
                produceUnits,
                realizedRevenue,          // Exact money already made
                totalUnsoldAssetValue,    // Value of physical assets waiting to be sold
                totalProjectedRevenue,    // The grand total of past + future income
                actualSunkCosts,          // Every kobo already spent
                projectedNetProfit,
                profitMargin
        );
    }

    // =========================================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================================

    private BatchFinancialSummaryDto buildBatchSummary(Batch batch, List<Transaction> farmTransactions) {
        List<Transaction> batchTxns = farmTransactions.stream()
                .filter(t -> t.getBatch() != null && t.getBatch().getId().equals(batch.getId()))
                .toList();

        double bRev = calculator.calculateTotalRevenue(batchTxns);
        double bExp = calculator.calculateTotalExpenses(batchTxns);
        int pop = determinePopulationForMath(batch);

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
    }

    private int determinePopulationForMath(Batch batch) {
        return batch.getCurrentCount() > 0 ? batch.getCurrentCount() : batch.getInitialCount();
    }

    private double calculateBatchWeight(Batch batch) {
        List<DailyLog> logs = dailyLogService.getLogEntitiesForBatch(batch.getId());
        double latestWeight = 0.0;

        // Iterate backwards to find the most recent valid weight recording
        for (int i = logs.size() - 1; i >= 0; i--) {
            if (logs.get(i).getAverageWeight() != null && logs.get(i).getAverageWeight() > 0) {
                latestWeight = logs.get(i).getAverageWeight();
                break;
            }
        }

        return batch.getCurrentCount() * latestWeight;
    }

    private double getFarmProduceStock(Farm farm) {
        try {
            Inventory eggInventory = inventoryService.getOrCreateEggInventory(farm);
            return eggInventory.getCurrentQuantity();
        } catch (Exception e) {
            return 0.0;
        }
    }
}