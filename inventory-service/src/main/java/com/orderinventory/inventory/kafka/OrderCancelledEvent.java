package com.orderinventory.inventory.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Consumer-side copy of the compensating event published by order-service on cancellation. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCancelledEvent {

    private Long orderId;
    private List<Item> items;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String productId;
        private int quantity;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
