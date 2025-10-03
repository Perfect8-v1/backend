package com.perfect8.admin.service;

import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    public String getInventory() {
        return "Inventory data";
    }

    public String updateStock(Long productId, Integer quantity) {
        return "Stock updated for product " + productId + ": " + quantity;
    }
}
