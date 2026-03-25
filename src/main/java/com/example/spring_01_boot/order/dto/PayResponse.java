package com.example.spring_01_boot.order.dto;

import java.time.Instant;

import com.example.spring_01_boot.order.repository.entity.PaymentType;
import com.example.spring_01_boot.order.repository.entity.PayStatus;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class PayResponse {
    private Long payId;
    private Long orderId;
    private PaymentType paymentType;
    private PayStatus payStatus;
    private Instant createdAt;
}
