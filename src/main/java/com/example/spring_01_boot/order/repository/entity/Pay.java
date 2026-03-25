package com.example.spring_01_boot.order.repository.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Pay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payId;

    private Long orderId;
    private PaymentType paymentType;
    private PayStatus payStatus;
    private String creditMethod;
    private int amount;
    private Instant createdAt;

    public Pay(Long orderId, PaymentType paymentType, PayStatus payStatus, String creditMethod, int amount) {
        this.orderId = orderId;
        this.paymentType = paymentType;
        this.payStatus = payStatus;
        this.creditMethod = creditMethod;
        this.amount = amount;
        this.createdAt = Instant.now();
    }

    public void cancel() {
        if (this.payStatus == PayStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 결제입니다.");
        }
        this.payStatus = PayStatus.CANCELED;
    }
}
