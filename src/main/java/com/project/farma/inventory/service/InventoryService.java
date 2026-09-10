package com.project.farma.inventory.service;

import com.project.farma.farm.model.Farm;
import com.project.farma.farm.service.FarmService;
import com.project.farma.inventory.dto.InventoryRequestDto;
import com.project.farma.inventory.dto.InventoryResponseDto;
import com.project.farma.inventory.dto.ProduceSaleRequestDto;
import com.project.farma.inventory.mapper.InventoryMapper;
import com.project.farma.inventory.model.Inventory;
import com.project.farma.inventory.model.InventoryCategory;
import com.project.farma.inventory.repository.InventoryRepository;
import com.project.farma.transaction.dto.InternalTransactionRequestDto;
import com.project.farma.transaction.dto.TransactionRequestDto;
import com.project.farma.transaction.model.TransactionCategory;
import com.project.farma.transaction.model.TransactionType;
import com.project.farma.transaction.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @Transactional
    public Inventory getOrCreateEggInventory(Farm farm) {
        Page<InventoryResponseDto> inventories = getInventoriesByFarm(farm.getId(), PageRequest.of(0, 1000));

        for (InventoryResponseDto dto : inventories.getContent()) {
            if ("Farm Eggs".equalsIgnoreCase(dto.name()) || InventoryCategory.PRODUCE.name().equals(dto.category())) {
                return getInventoryEntityById(dto.id());
            }
        }

        Inventory eggInventory = new Inventory();
        eggInventory.setName("Farm Eggs");
        eggInventory.setCategory(InventoryCategory.PRODUCE);
        eggInventory.setCurrentQuantity(0.0);
        eggInventory.setUnit("Units");
        eggInventory.setUnitPrice(0.0);
        eggInventory.setLowStockThreshold(0.0);
        eggInventory.setExpiryDate(LocalDate.now().plusDays(21));
        eggInventory.setFarm(farm);
        eggInventory.setOrganisation(farm.getOrganisation());

        return inventoryRepository.save(eggInventory);
    }

    @Transactional
    public void recordProduceSale(ProduceSaleRequestDto requestDto) {
        Inventory inventory = getInventoryEntityById(requestDto.inventoryId());

        if (inventory.getCategory() != InventoryCategory.PRODUCE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can only sell items categorized as PRODUCE from this endpoint.");
        }

        checkSufficientStock(inventory, inventory.getCurrentQuantity() - requestDto.quantitySold(), -requestDto.quantitySold());

        inventory.setCurrentQuantity(inventory.getCurrentQuantity() - requestDto.quantitySold());
        inventoryRepository.save(inventory);

        double totalRevenue = requestDto.quantitySold() * requestDto.unitPrice();

        if (totalRevenue > 0) {
            String notes = requestDto.notes() != null && !requestDto.notes().isBlank() ? requestDto.notes() : "N/A";

            String auditNarrative = String.format("Sold %.2f %s of %s @ ₦%.2f per unit. Notes: %s",
                    requestDto.quantitySold(), inventory.getUnit(), inventory.getName(), requestDto.unitPrice(), notes);

            transactionService.createInternalTransaction(new InternalTransactionRequestDto(
                    inventory.getOrganisation().getId(),
                    null, // Null because it's a warehouse sale, not a specific bird batch
                    inventory.getFarm().getId(), // Proper farm ID passed
                    totalRevenue,
                    TransactionType.CREDIT,
                    TransactionCategory.PRODUCE_SALE,
                    auditNarrative
            ));
        }
    }

    public Page<InventoryResponseDto> getInventoriesByFarm(Long farmId, Pageable pageable) {
        return inventoryRepository.findByFarmId(farmId, pageable)
                .map(inventoryMapper::toInventoryResponseDto);
    }

    public Inventory getInventoryEntityById(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with ID: " + inventoryId));
    }

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