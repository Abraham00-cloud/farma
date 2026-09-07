package com.project.farma.inventory.service;

import com.project.farma.farm.model.Farm;
import com.project.farma.farm.service.FarmService;
import com.project.farma.inventory.dto.InventoryRequestDto;
import com.project.farma.inventory.dto.InventoryResponseDto;
import com.project.farma.inventory.mapper.InventoryMapper;
import com.project.farma.inventory.model.Inventory;
import com.project.farma.inventory.model.InventoryCategory;
import com.project.farma.inventory.repository.InventoryRepository;
import com.project.farma.transaction.dto.TransactionRequestDto;
import com.project.farma.transaction.model.TransactionCategory;
import com.project.farma.transaction.model.TransactionType;
import com.project.farma.transaction.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final FarmService farmService;
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final TransactionService transactionService;

    @Transactional
    public InventoryResponseDto createInventory(InventoryRequestDto requestDto) {
        Farm farm = farmService.getFarmById(requestDto.farmId());

        Inventory inventory = inventoryMapper.toInventoryEntity(requestDto);
        inventory.setFarm(farm);
        inventory.setOrganisation(farm.getOrganisation());

        Inventory savedInventory = inventoryRepository.save(inventory);

        logInitialPurchaseTransaction(requestDto, farm);

        return inventoryMapper.toInventoryResponseDto(savedInventory);
    }

    @Transactional
    public void updateStockLevel(Long inventoryId, Double adjustmentAmount) {
        Inventory inventory = getInventoryEntityById(inventoryId);
        Double newStock = inventory.getCurrentQuantity() + adjustmentAmount;

        checkSufficientStock(inventory, newStock, adjustmentAmount);

        inventory.setCurrentQuantity(newStock);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public InventoryResponseDto restockInventory(Long inventoryId, Double addedQuantity, Double newUnitPrice) {
        Inventory inventory = getInventoryEntityById(inventoryId);

        checkRestockParameters(addedQuantity, newUnitPrice);

        double newBlendedUnitPrice = calculateBlendedUnitPrice(inventory, addedQuantity, newUnitPrice);
        double newTotalQuantity = inventory.getCurrentQuantity() + addedQuantity;

        inventory.setCurrentQuantity(newTotalQuantity);
        inventory.setUnitPrice(newBlendedUnitPrice);
        Inventory savedInventory = inventoryRepository.save(inventory);

        logRestockTransaction(inventory, addedQuantity, newUnitPrice);

        return inventoryMapper.toInventoryResponseDto(savedInventory);
    }

    public Page<InventoryResponseDto> getInventoriesByFarm(Long farmId, Pageable pageable) {
        return inventoryRepository.findByFarmId(farmId, pageable)
                .map(inventoryMapper::toInventoryResponseDto);
    }

    public Inventory getInventoryEntityById(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with ID: " + inventoryId));
    }

    // PRIVATE HELPER METHODS

    private void checkSufficientStock(Inventory inventory, Double newStock, Double adjustmentAmount) {
        if (newStock < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Insufficient stock for %s. Available: %.2f %s, Requested deduction: %.2f %s",
                            inventory.getName(), inventory.getCurrentQuantity(), inventory.getUnit(), Math.abs(adjustmentAmount), inventory.getUnit()
                    )
            );
        }
    }

    private void checkRestockParameters(Double addedQuantity, Double newUnitPrice) {
        if (addedQuantity <= 0 || newUnitPrice <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Restock quantity and unit price must be strictly greater than zero.");
        }
    }

    private double calculateBlendedUnitPrice(Inventory inventory, Double addedQuantity, Double newUnitPrice) {
        double currentTotalValue = inventory.getCurrentQuantity() * inventory.getUnitPrice();
        double newPurchaseValue = addedQuantity * newUnitPrice;
        double newTotalQuantity = inventory.getCurrentQuantity() + addedQuantity;

        if (newTotalQuantity <= 0) {
            return newUnitPrice;
        }
        return (currentTotalValue + newPurchaseValue) / newTotalQuantity;
    }

    private void logInitialPurchaseTransaction(InventoryRequestDto requestDto, Farm farm) {
        double totalCost = requestDto.quantity() * requestDto.unitPrice();
        if (totalCost <= 0) return;

        TransactionCategory txnCategory = mapInventoryToTransactionCategory(requestDto.category());
        String auditNarrative = String.format("Auto-logged purchase of %.2f %s of %s",
                requestDto.quantity(), requestDto.unit(), requestDto.name());

        TransactionRequestDto expenseLog = new TransactionRequestDto(
                totalCost, TransactionType.DEBIT, txnCategory, auditNarrative,
                LocalDate.now(), true, farm.getOrganisation().getId(), farm.getId(), null
        );
        transactionService.createTransaction(expenseLog);
    }

    private void logRestockTransaction(Inventory inventory, Double addedQuantity, Double newUnitPrice) {
        double newPurchaseValue = addedQuantity * newUnitPrice;
        TransactionCategory txnCategory = mapInventoryToTransactionCategory(inventory.getCategory());

        String auditNarrative = String.format("Restocked %.2f %s of %s at ₦%.2f/unit",
                addedQuantity, inventory.getUnit(), inventory.getName(), newUnitPrice);

        TransactionRequestDto expenseLog = new TransactionRequestDto(
                newPurchaseValue, TransactionType.DEBIT, txnCategory, auditNarrative,
                LocalDate.now(), true, inventory.getOrganisation().getId(), inventory.getFarm().getId(), null
        );
        transactionService.createTransaction(expenseLog);
    }

    private TransactionCategory mapInventoryToTransactionCategory(InventoryCategory invCategory) {
        return switch (invCategory) {
            case FEED -> TransactionCategory.FEED_PURCHASE;
            case MEDICINE -> TransactionCategory.MEDICINE_PURCHASE;
            case VACCINE -> TransactionCategory.VACCINE_PURCHASE;
            case EQUIPMENT -> TransactionCategory.EQUIPMENT_PURCHASE;
            default -> TransactionCategory.OTHER_EXPENSE;
        };
    }
}