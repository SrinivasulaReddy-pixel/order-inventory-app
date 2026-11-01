package com.orderinventory.notification.kafka;

import com.orderinventory.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final NotificationService notificationService;

    public OrderEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Separate consumer group from inventory-service -> both services receive every event independently.
    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent for orderId={}", event.getOrderId());
        notificationService.sendOrderConfirmation(event.getCustomerUsername(), event.getOrderId());
    }
}
