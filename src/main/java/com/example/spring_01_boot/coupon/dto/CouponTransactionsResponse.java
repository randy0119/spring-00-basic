package com.example.spring_01_boot.coupon.dto;

import java.util.List;

/**
 * {@code {"userId": "...", "transactions": [...]}}
 */
public record CouponTransactionsResponse(String userId, List<CouponTransactionItemResponse> transactions) {}
