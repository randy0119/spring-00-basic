package com.example.spring_01_boot.point.dto;

import java.util.List;

/**
 * {@code {"userId": "...", "transactions": [...]}}
 */
public record PointTransactionsResponse(String userId, List<PointTransactionItemResponse> transactions) {}
