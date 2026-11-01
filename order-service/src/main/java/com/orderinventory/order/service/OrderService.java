package com.orderinventory.order.service;

import com.orderinventory.order.client.InventoryCheckService;
import com.orderinventory.order.dto.CreateOrderRequest;
import com.orderinventory.order.dto.OrderItemRequest;
import com.orderinventory.order.kafka.OrderEventProducer;
import com.orderinventory.order.kafka.OrderPlacedEvent;
import com.orderinventory.order.model.Order;
import com.orderinventory.order.model.OrderItem;
import com.orderinventory.order.model.OrderStatus;
import com.orderinventory.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryCheckService inventoryCheckService;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository, InventoryCheckService inventoryCheckService,
                         OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.inventoryCheckService = inventoryCheckService;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    public Order placeOrder(String username, CreateOrderRequest request) {

        // SYNCHRONOUS step: confirm stock is available right now, so we can give
        // the customer an immediate yes/no before committing anything.
        for (OrderItemRequest item : request.getItems()) {
            boolean available = inventoryCheckService.isAvailable(item.getProductId(), item.getQuantity());
            if (!available) {
                throw new IllegalStateException("Insufficient stock for product " + item.getProductId());
            }
        }

        Order order = new Order();
        order.setCustomerUsername(username);
        order.setStatus(OrderStatus.PLACED);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = new OrderItem(itemReq.getProductId(), itemReq.getProductName(),
                    itemReq.getQuantity(), itemReq.getPrice());
            order.addItem(item);
            total = total.add(itemReq.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        // ASYNCHRONOUS step: tell the rest of the system an order was placed.
        // Inventory decrements real stock; Notification sends a confirmation.
        // Order doesn't wait on either of them.
        List<OrderPlacedEvent.Item> eventItems = request.getItems().stream()
                .map(i -> new OrderPlacedEvent.Item(i.getProductId(), i.getQuantity(), i.getPrice()))
                .collect(Collectors.toList());
        orderEventProducer.publishOrderPlaced(new OrderPlacedEvent(saved.getId(), username, eventItems));

        return saved;
    }

    public List<Order> getOrdersForUser(String username) {
        return orderRepository.findByCustomerUsername(username);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
