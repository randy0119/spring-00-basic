package com.example.spring_01_boot.order.service;

import com.example.spring_01_boot.order.dto.PayResponse;
import com.example.spring_01_boot.order.repository.entity.PaymentType;
import com.example.spring_01_boot.order.repository.entity.PayStatus;

public interface PayService {
    PayResponse pay(Long orderId, PaymentType paymentType, String creditMethod, int amount);
    PayResponse cancel(Long orderId);

    Long getPayIdFromOrderIdWherePayStatus(Long orderId, PayStatus payStatus);
}
