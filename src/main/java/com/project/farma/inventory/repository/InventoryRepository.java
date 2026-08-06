package com.project.farma.inventory.repository;

import com.project.farma.inventory.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByFarmId(Long farmId);
}
