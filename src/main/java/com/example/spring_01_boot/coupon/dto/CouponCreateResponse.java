package com.example.spring_01_boot.coupon.dto;

import com.example.spring_01_boot.coupon.repository.entity.CouponStatus;

import java.time.Instant;

public record CouponCreateResponse(
    String couponId,
    String name,
    int totalQuantity,
    int issuedQuantity,
    Instant expiresAt,
    CouponStatus status,
    Instant createdAt
) {
}

