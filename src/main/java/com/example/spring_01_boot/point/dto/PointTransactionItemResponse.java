package com.example.spring_01_boot.point.dto;

import java.time.Instant;

/**
 * 거래 내역 한 건 (JSON 직렬화용).
 */
public record PointTransactionItemResponse(
    long transactionId,
    String type,
    long amount,
    long balanceAfter,
    Instant createdAt
) {}
