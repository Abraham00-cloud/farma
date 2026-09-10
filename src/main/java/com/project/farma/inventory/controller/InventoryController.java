package com.project.farma.inventory.controller;


import com.project.farma.inventory.dto.InventoryRequestDto;
import com.project.farma.inventory.dto.InventoryResponseDto;
import com.project.farma.inventory.dto.ProduceSaleRequestDto;
import com.project.farma.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
@Tag(
        name = "8. Inventory Management",
        description = "Secure endpoints to manage corporate farm assets, track feed stock balances, and monitor medicinal supplies"
)
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Onboard New Inventory Supply Item",
            description = "Registers a new material resource (Feed, Vaccine, Equipment) and attaches it to both a physical farm location and the root organization tenant."
    )
    public ResponseEntity<InventoryResponseDto> createInventory(@Valid @RequestBody InventoryRequestDto requestDto) {
        InventoryResponseDto inventory = inventoryService.createInventory(requestDto);
        return new ResponseEntity<>(inventory, HttpStatus.CREATED);
    }

    @PatchMapping("/{inventoryId}/stock")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Manually Adjust Item Stock Levels",
            description = "Modifies inventory count levels directly. Use positive values for restocks, and negative values for standard stock reductions."
    )
    public ResponseEntity<Void> updateStockLevel(
            @PathVariable Long inventoryId,
            @RequestParam Double adjustmentAmount
    ) {
        inventoryService.updateStockLevel(inventoryId, adjustmentAmount);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{inventoryId}/restock")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Restock existing inventory (Applies Weighted Average Costing)")
    public ResponseEntity<InventoryResponseDto> restockInventory(
            @PathVariable Long inventoryId,
            @RequestParam Double addedQuantity,
            @RequestParam Double newUnitPrice
    ) {
        InventoryResponseDto updated = inventoryService.restockInventory(inventoryId, addedQuantity, newUnitPrice);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/produce/sale")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(
            summary = "Record Produce Sale",
            description = "Deducts farm produce (e.g., eggs) from inventory and automatically logs the financial revenue into the corporate transaction ledger."
    )
    public ResponseEntity<Void> recordProduceSale(@Valid @RequestBody ProduceSaleRequestDto requestDto) {
        inventoryService.recordProduceSale(requestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/farm/{farmId}")
    @PreAuthorize("hasAnyRole('PROPRIETOR', 'MANAGER')")
    @Operation(summary = "Fetch Inventory Stock Items for a Farm Facility")
    public ResponseEntity<Page<InventoryResponseDto>> getInventoriesByFarm(
            @PathVariable Long farmId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());
        Page<InventoryResponseDto> items = inventoryService.getInventoriesByFarm(farmId, pageable);
        return ResponseEntity.ok(items);
    }
}