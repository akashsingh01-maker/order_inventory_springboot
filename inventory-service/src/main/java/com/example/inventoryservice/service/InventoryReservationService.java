package com.example.inventoryservice.service;

import com.example.inventoryservice.persistence.ProductEntity;
import com.example.inventoryservice.persistence.ProductRepository;
import com.example.inventoryservice.dto.ReservationItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryReservationService {

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public void reserveForOrder(String orderId, List<ReservationItem> items) {
        // lock product rows in consistent order
        items.stream().sorted((a,b)->a.getProductId().compareTo(b.getProductId())).forEach(item -> {
            Long pid = item.getProductId();
            ProductEntity p = productRepository.findByIdForUpdate(pid).orElseThrow(() -> new RuntimeException("product not found"));
            if (p.getAvailable() < item.getQuantity()) throw new RuntimeException("insufficient inventory");
            p.setAvailable(p.getAvailable() - item.getQuantity());
            p.setReserved(p.getReserved() + item.getQuantity());
            productRepository.save(p);
        });
    }
}
