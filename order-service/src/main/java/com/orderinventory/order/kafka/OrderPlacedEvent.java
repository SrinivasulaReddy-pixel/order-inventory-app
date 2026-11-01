package com.orderinventory.order.kafka;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event contract published to the "order-events" Kafka topic.
 * Consumers (inventory-service, notification-service) each keep their own copy
 * of this shape - deliberately not a shared library class, so services stay
 * decoupled and can evolve independently (only the JSON contract is shared).
 */
public class OrderPlacedEvent {

    private Long orderId;
    private String customerUsername;
    private List<Item> items;

    public OrderPlacedEvent() {}

    public OrderPlacedEvent(Long orderId, String customerUsername, List<Item> items) {
        this.orderId = orderId;
        this.customerUsername = customerUsername;
        this.items = items;
    }

    public Long getOrderId() { return orderId; }
    public String getCustomerUsername() { return customerUsername; }
    public List<Item> getItems() { return items; }

    public static class Item {
        private String productId;
        private int quantity;
        private BigDecimal price;

        public Item() {}

        public Item(String productId, int quantity, BigDecimal price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }

        public String getProductId() { return productId; }
        public int getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; }
    }
}
