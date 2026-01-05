package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.InventoryDto;
import com.example.inventoryservice.persistence.ProductEntity;
import com.example.inventoryservice.persistence.ProductRepository;
import com.example.inventoryservice.service.InventoryReservationService;
import com.example.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private InventoryReservationService reservationService;

    @GetMapping
    public List<ProductEntity> all() {
        return repository.findAll();
    }

    @PostMapping
    public ProductEntity save(@RequestBody ProductEntity p) {
        return repository.save(p);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        Optional<ProductEntity> p = repository.findById(id);
        if (p.isPresent()) {
            InventoryDto dto = new InventoryDto(p.get().getId().toString(), p.get().getAvailable(), p.get().getReserved(), java.time.Instant.now().toString());
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(404).body("not found");
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<?> reserve(@PathVariable Long id, @RequestBody java.util.Map<String,Integer> body) {
        int qty = body.getOrDefault("quantity", 0);
        // perform reservation in a transaction with pessimistic locks
        try {
            com.example.inventoryservice.dto.ReservationItem item = new com.example.inventoryservice.dto.ReservationItem(id, qty);
            reservationService.reserveForOrder("external", java.util.Arrays.asList(item));
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(409).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<?> release(@PathVariable Long id, @RequestBody java.util.Map<String,Integer> body) {
        int qty = body.getOrDefault("quantity", 0);
        // simplistic release: increase available, decrease reserved
        ProductEntity p = repository.findById(id).orElseThrow(() -> new RuntimeException("product not found"));
        p.setAvailable(p.getAvailable() + qty);
        p.setReserved(Math.max(0, p.getReserved() - qty));
        repository.save(p);
        return ResponseEntity.ok().build();
    }
}
