package com.example.spring_01_boot.order.service;

import com.example.spring_01_boot.order.dto.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(String userId, String bascketId);

    List<OrderResponse> getOrders(String userId);
}
