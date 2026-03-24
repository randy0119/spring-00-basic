package com.example.spring_01_boot.coupon.dto;

import java.time.Instant;

/**
 * 거래 내역 한 건 (JSON 직렬화용).
 */
public record CouponTransactionItemResponse(
    long transactionId,
    String couponName,
    Instant timestamp
) {}
