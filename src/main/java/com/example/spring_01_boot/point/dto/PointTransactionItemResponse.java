package com.example.spring_01_boot.point.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * 거래 내역 한 건 (JSON 직렬화용).
 */
@Getter
@AllArgsConstructor
public class PointTransactionItemResponse{
    private long transactionId;
    private String type;
    private long amount;
    private long balanceAfter;
    private Instant createdAt;
}
