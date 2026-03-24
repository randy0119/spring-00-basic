package com.example.spring_01_boot.coupon.dto;

import java.time.Instant;

public record CouponIssueResponse(
    String userId,
    String couponId,
    String userCouponId,
    String status,
    Instant issuedAt,
    Instant expiresAt
) {
}
