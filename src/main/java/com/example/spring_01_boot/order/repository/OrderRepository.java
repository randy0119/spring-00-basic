package com.example.spring_01_boot.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.spring_01_boot.order.repository.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByBascketId(String bascketId);
}
