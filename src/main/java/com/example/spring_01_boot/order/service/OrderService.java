package com.example.spring_01_boot.order.service;

import com.example.spring_01_boot.order.dto.OrderResponse;
import com.example.spring_01_boot.order.repository.entity.Order;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(String userId, String bascketId);

    List<OrderResponse> getOrders(String userId);

    OrderResponse getOrder(Long orderId);

    void pay(Long orderId);

    void cancel(Long orderId);
}
