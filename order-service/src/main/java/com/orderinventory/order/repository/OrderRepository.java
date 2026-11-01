package com.orderinventory.order.repository;

import com.orderinventory.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerUsername(String customerUsername);
}
