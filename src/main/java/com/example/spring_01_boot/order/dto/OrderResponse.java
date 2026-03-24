package com.example.spring_01_boot.order.dto;

import java.time.Instant;

import com.example.spring_01_boot.order.repository.entity.OrderStatus;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String userId;
    private String bascketId;
    private OrderStatus orderStatus;
    private Instant createdAt;
}
