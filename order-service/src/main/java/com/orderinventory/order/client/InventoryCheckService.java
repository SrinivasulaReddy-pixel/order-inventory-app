package com.orderinventory.order.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventoryCheckService {

    private static final Logger log = LoggerFactory.getLogger(InventoryCheckService.class);

    private final InventoryClient inventoryClient;

    public InventoryCheckService(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    /**
     * Synchronous stock check. Wrapped in a circuit breaker: if inventory-service
     * is down/slow, we fail fast instead of blocking the order flow indefinitely.
     */
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackCheckStock")
    public boolean isAvailable(String productId, int qty) {
        return inventoryClient.checkStock(productId, qty);
    }

    public boolean fallbackCheckStock(String productId, int qty, Throwable t) {
        log.warn("inventory-service unavailable (circuit open/error) for productId={}: {}", productId, t.getMessage());
        return false; // fail-safe: treat as "not available" rather than placing an order we can't fulfil
    }
}
