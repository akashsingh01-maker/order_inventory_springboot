package com.example.inventoryservice.service;

import com.example.inventoryservice.model.Product;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InventoryService {
    public boolean tryReserve(UUID productId, int qty) {
        // implement DB transactional reserve logic in real app
        return true;
    }
}
