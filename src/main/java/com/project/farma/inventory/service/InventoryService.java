package com.project.farma.inventory.service;

import com.project.farma.farm.model.Farm;
import com.project.farma.farm.service.FarmService;
import com.project.farma.inventory.dto.InventoryRequestDto;
import com.project.farma.inventory.dto.InventoryResponseDto;
import com.project.farma.inventory.mapper.InventoryMapper;
import com.project.farma.inventory.model.Inventory;
import com.project.farma.inventory.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final FarmService farmService;
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Transactional
    public InventoryResponseDto createInventory(InventoryRequestDto requestDto) {
        Farm farm = farmService.getFarmById(requestDto.farmId());

        Inventory inventory = inventoryMapper.toInventoryEntity(requestDto);
        inventory.setFarm(farm);
        inventory.setOrganisation(farm.getOrganisation());

        Inventory savedInventory = inventoryRepository.save(inventory);

        return inventoryMapper.toInventoryResponseDto(savedInventory);
    }

    @Transactional
    public void updateStockLevel(Long inventoryId, Double adjustmentAmount) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found"));

        Double currentStock = inventory.getCurrentQuantity();
        Double newStock = currentStock + adjustmentAmount;

        if (newStock < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("insufficient stock for %s. Available: %.2f  %s, Requested deduction: %.2f %s",
                            inventory.getName(),
                            currentStock,
                            inventory.getUnit(),
                            Math.abs(adjustmentAmount),
                            inventory.getUnit()
                            )
            );
        }
        inventory.setCurrentQuantity(newStock);
        inventoryRepository.save(inventory);
    }

    public Inventory getInventoryEntityById(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory not found"));
    }
}
