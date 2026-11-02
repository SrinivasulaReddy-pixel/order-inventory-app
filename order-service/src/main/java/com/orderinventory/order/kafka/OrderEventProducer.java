package com.orderinventory.order.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    public static final String TOPIC = "order-events";
    public static final String CANCEL_TOPIC = "order-cancelled-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderPlaced(OrderPlacedEvent event) {
        log.info("Publishing OrderPlacedEvent for orderId={} to topic={}", event.getOrderId(), TOPIC);
        // key = orderId so events for the same order always land on the same partition
        kafkaTemplate.send(TOPIC, String.valueOf(event.getOrderId()), event);
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        log.info("Publishing OrderCancelledEvent for orderId={} to topic={}", event.getOrderId(), CANCEL_TOPIC);
        kafkaTemplate.send(CANCEL_TOPIC, String.valueOf(event.getOrderId()), event);
    }
}
