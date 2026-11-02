package com.orderinventory.inventory.kafka;

import com.orderinventory.inventory.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final InventoryService inventoryService;

    public OrderEventConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent for orderId={}", event.getOrderId());
        for (OrderPlacedEvent.Item item : event.getItems()) {
            inventoryService.decrementStock(item.getProductId(), item.getQuantity());
        }
    }

    // Separate deserializer target type for this topic - override the consumer factory's
    // default JSON type just for this listener, since "order-events" and
    // "order-cancelled-events" carry different payload shapes.
    @KafkaListener(
            topics = "order-cancelled-events",
            groupId = "inventory-service",
            properties = "spring.json.value.default.type=com.orderinventory.inventory.kafka.OrderCancelledEvent"
    )
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent for orderId={} - restocking", event.getOrderId());
        for (OrderCancelledEvent.Item item : event.getItems()) {
            inventoryService.restock(item.getProductId(), item.getQuantity());
        }
    }
}
