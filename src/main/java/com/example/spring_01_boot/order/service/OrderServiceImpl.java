package com.example.spring_01_boot.order.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.spring_01_boot.order.repository.OrderRepository;
import com.example.spring_01_boot.order.repository.entity.Order;
import com.example.spring_01_boot.order.dto.OrderResponse;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public OrderResponse createOrder(String userId, String bascketId) {
        Order existingOrder = orderRepository.findByBascketId(bascketId);
        if (existingOrder != null) {
            throw new IllegalArgumentException("이미 주문된 장바구니입니다.");
        }
        Order order = orderRepository.save(new Order(userId, bascketId));
        return new OrderResponse(order.getOrderId(), order.getUserId(), order.getBascketId(), order.getOrderStatus(), order.getCreatedAt());
    }
}
