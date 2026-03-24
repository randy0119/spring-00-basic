package com.example.spring_01_boot.order.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import com.example.spring_01_boot.order.service.OrderService;
import com.example.spring_01_boot.order.dto.OrderResponse;
import com.example.spring_01_boot.order.dto.OrderCreateRequest;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/order/new")
    public OrderResponse createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return orderService.createOrder(request.getUserId(), request.getBascketId());
    }
}
