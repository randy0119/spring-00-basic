package com.example.spring_01_boot.order.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private String userId;
    private String bascketId;
    private OrderStatus orderStatus;
    private Instant createdAt;

    public Order(String userId, String bascketId) {
        this.userId = userId;
        this.bascketId = bascketId;
        this.orderStatus = OrderStatus.CREATED;
        this.createdAt = Instant.now();
    }

    public void pay() {
        if (this.orderStatus == OrderStatus.CANCELED) {
            throw new IllegalStateException("주문 상태가 올바르지 않습니다.");
        }

        if (this.orderStatus == OrderStatus.PAID) {
            throw new IllegalStateException("이미 결제된 주문입니다.");
        }
        this.orderStatus = OrderStatus.PAID;
    }

    public void cancel() {
        if (this.orderStatus == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        this.orderStatus = OrderStatus.CANCELED;
    }
}
