package com.project.farma.inventory.mapper;

import com.project.farma.inventory.dto.InventoryRequestDto;
import com.project.farma.inventory.dto.InventoryResponseDto;
import com.project.farma.inventory.model.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {
    public Inventory toInventoryEntity(InventoryRequestDto requestDto) {
        return Inventory.builder()
                .name(requestDto.name())
                .category(requestDto.category())
                .currentQuantity(requestDto.quantity())
                .unit(requestDto.unit())
                .unitPrice(requestDto.unitPrice())
                .lowStockThreshold(requestDto.lowStockThreshold() != null ? requestDto.lowStockThreshold() : 0.0)
                .build();
    }

    public InventoryResponseDto toInventoryResponseDto(Inventory inventory) {

        double totalValue = inventory.getCurrentQuantity() * inventory.getUnitPrice();

        boolean isLowStock = inventory.getCurrentQuantity() <= inventory.getLowStockThreshold();

        return new InventoryResponseDto(
                inventory.getId(),
                inventory.getName(),
                inventory.getCategory().name(),
                inventory.getFarm().getId(),
                inventory.getFarm().getName(),
                inventory.getCurrentQuantity(),
                inventory.getUnitPrice().toString(),
                totalValue,
                inventory.getExpiryDate(),
                inventory.getLowStockThreshold(),
                isLowStock



        );
    }
}
