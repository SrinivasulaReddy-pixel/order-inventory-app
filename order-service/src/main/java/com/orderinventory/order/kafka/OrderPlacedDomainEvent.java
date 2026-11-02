package com.orderinventory.order.kafka;

/**
 * Internal Spring application event - NOT what goes on the Kafka wire (that's
 * OrderPlacedEvent). Published from inside the @Transactional service method;
 * actually sent to Kafka only after the surrounding DB transaction commits
 * (see OrderEventRelay). This avoids the dual-write problem: publishing to Kafka
 * directly inside the transactional method risks the message going out even if
 * the DB write that triggered it later fails to commit.
 */
public class OrderPlacedDomainEvent {
    private final OrderPlacedEvent payload;

    public OrderPlacedDomainEvent(OrderPlacedEvent payload) {
        this.payload = payload;
    }

    public OrderPlacedEvent getPayload() { return payload; }
}
