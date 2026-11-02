package com.orderinventory.order.kafka;

/** See OrderPlacedDomainEvent - same reasoning, for the cancellation/compensation path. */
public class OrderCancelledDomainEvent {
    private final OrderCancelledEvent payload;

    public OrderCancelledDomainEvent(OrderCancelledEvent payload) {
        this.payload = payload;
    }

    public OrderCancelledEvent getPayload() { return payload; }
}
