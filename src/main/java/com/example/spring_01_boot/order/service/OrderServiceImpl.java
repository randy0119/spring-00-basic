package com.example.spring_01_boot.order.service;

import com.example.spring_01_boot.order.dto.OrderResponse;
import com.example.spring_01_boot.order.repository.OrderRepository;
import com.example.spring_01_boot.order.repository.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return new OrderResponse(
            order.getOrderId(),
            order.getUserId(),
            order.getBascketId(),
            order.getOrderStatus(),
            order.getCreatedAt()
        );
    }

    @Override
    public List<OrderResponse> getOrders(String userId) {
        List<Order> orders = orderRepository.findAllByUserId(userId);
        return orders.stream()
            .map(order -> new OrderResponse(
                order.getOrderId(),
                order.getUserId(),
                order.getBascketId(),
                order.getOrderStatus(),
                order.getCreatedAt()
            ))
            .toList();
    }
}
