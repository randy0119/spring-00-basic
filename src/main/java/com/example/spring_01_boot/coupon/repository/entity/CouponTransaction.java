package com.example.spring_01_boot.coupon.repository.entity;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String couponName;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Instant timestamp;

    public CouponTransaction(String couponName, String userId, Instant timestamp) {
        this.couponName = couponName;
        this.userId = userId;
        this.timestamp = timestamp;
    }
}
