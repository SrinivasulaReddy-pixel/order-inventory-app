package com.orderinventory.inventory.controller;

import com.orderinventory.inventory.dto.ProductRequest;
import com.orderinventory.inventory.model.Product;
import com.orderinventory.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(inventoryService.create(request));
    }

    /**
     * SYNCHRONOUS endpoint called directly by order-service before an order is confirmed.
     * Any authenticated caller (customer or admin) can hit this - the JWT forwarded by
     * order-service's Feign client is validated here independently.
     */
    @GetMapping("/{productId}/check")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public boolean checkStock(@PathVariable String productId, @RequestParam int qty) {
        return inventoryService.isAvailable(productId, qty);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> update(@PathVariable String productId, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(inventoryService.update(productId, request));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String productId) {
        inventoryService.delete(productId);
        return ResponseEntity.noContent().build();
    }
}
