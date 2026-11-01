package com.orderinventory.notification.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Consumer-side copy of the event contract published by order-service to "order-events". */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderPlacedEvent {

    private Long orderId;
    private String customerUsername;
    private List<Item> items;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }
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
