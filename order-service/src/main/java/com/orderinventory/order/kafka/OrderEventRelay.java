package com.orderinventory.order.kafka;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * The only place that actually talks to Kafka for order lifecycle events.
 * AFTER_COMMIT means: if the SQL transaction that raised this event rolls back for
 * any reason (constraint violation, concurrent update, whatever), nothing is ever
 * published - the database and the message broker can't drift apart because of a
 * failed local write.
 */
@Component
public class OrderEventRelay {

    private final OrderEventProducer orderEventProducer;

    public OrderEventRelay(OrderEventProducer orderEventProducer) {
        this.orderEventProducer = orderEventProducer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedDomainEvent event) {
        orderEventProducer.publishOrderPlaced(event.getPayload());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCancelled(OrderCancelledDomainEvent event) {
        orderEventProducer.publishOrderCancelled(event.getPayload());
    }
}
