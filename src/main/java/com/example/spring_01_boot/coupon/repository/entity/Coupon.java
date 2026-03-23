package com.example.spring_01_boot.coupon.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String couponName;

    @Column(nullable = false)
    @Positive
    private int totalQuantity;

    @Column(nullable = false)
    @PositiveOrZero
    private int issuedQuantity;

    @Column(nullable = false)
    private Instant expireDate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Coupon(String couponName, int totalQuantity, int issuedQuantity, Instant expireDate) {
        this(couponName, totalQuantity, issuedQuantity, expireDate, Instant.now());
    }

    public Coupon(String couponName, int totalQuantity, int issuedQuantity, Instant expireDate, Instant createdAt) {
        this.couponName = couponName;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = issuedQuantity;
        this.expireDate = expireDate;
        this.createdAt = createdAt;
    }

    /**
     * 쿠폰 상태는 "조회 시점 기준"으로 판정한다.
     * - expiresAt <= now  => EXPIRED
     * - issuedQuantity >= totalQuantity => SOLD_OUT
     * - 그 외 => AVAILABLE
     */
    public CouponStatus getStatus(Instant now) {
        if (expireDate != null && !now.isBefore(expireDate)) {
            return CouponStatus.EXPIRED;
        }
        if (issuedQuantity >= totalQuantity) {
            return CouponStatus.SOLD_OUT;
        }
        return CouponStatus.AVAILABLE;
    }
}