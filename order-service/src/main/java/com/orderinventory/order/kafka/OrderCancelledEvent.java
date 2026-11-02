package com.orderinventory.order.kafka;

import java.util.List;

/**
 * Published when a PLACED order is cancelled. inventory-service consumes this to
 * restock the items that were decremented when the order was originally placed -
 * a compensating action (Saga pattern), since there's no distributed transaction
 * tying the original stock decrement to this order.
 */
public class OrderCancelledEvent {

    private Long orderId;
    private List<Item> items;

    public OrderCancelledEvent() {}

    public OrderCancelledEvent(Long orderId, List<Item> items) {
        this.orderId = orderId;
        this.items = items;
    }

    public Long getOrderId() { return orderId; }
    public List<Item> getItems() { return items; }

    public static class Item {
        private String productId;
        private int quantity;

        public Item() {}

        public Item(String productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public String getProductId() { return productId; }
        public int getQuantity() { return quantity; }
    }
}
