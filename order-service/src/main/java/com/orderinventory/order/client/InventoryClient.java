package com.orderinventory.order.client;

import com.orderinventory.order.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Declarative REST client (Feign) for the SYNCHRONOUS call from Order -> Inventory.
 * "inventory-service" is resolved via Eureka (client-side load balancing).
 */
@FeignClient(name = "inventory-service", configuration = FeignClientConfig.class)
public interface InventoryClient {

    @GetMapping("/api/inventory/{productId}/check")
    boolean checkStock(@PathVariable("productId") String productId, @RequestParam("qty") int qty);
}
