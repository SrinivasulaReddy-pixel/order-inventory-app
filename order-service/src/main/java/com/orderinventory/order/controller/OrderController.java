package com.orderinventory.order.controller;

import com.orderinventory.order.dto.CreateOrderRequest;
import com.orderinventory.order.model.Order;
import com.orderinventory.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody CreateOrderRequest request, Authentication auth) {
        Order order = orderService.placeOrder(auth.getName(), request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<List<Order>> myOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getOrdersForUser(auth.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> allOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Order order = orderService.cancelOrder(auth.getName(), isAdmin, id);
        return ResponseEntity.ok(order);
    }
}
