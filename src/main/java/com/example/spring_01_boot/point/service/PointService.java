package com.example.spring_01_boot.point.service;

import com.example.spring_01_boot.point.dto.PointOperationResponse;
import com.example.spring_01_boot.point.dto.PointTransactionsResponse;

/**
 * 포인트 조회·충전·사용.
 * 충전/사용은 성공 시 {@link PointOperationResponse#balance()}, 실패 시 {@link PointOperationResponse#message()}를 확인합니다.
 */
public interface PointService {

    int getPoint(String userId);

    PointOperationResponse chargePoint(String userId, int amount);

    PointOperationResponse usePoint(String userId, int amount);

    /** 거래 내역 최신순, {@code limit} 건까지 */
    PointTransactionsResponse getTransactions(String userId, int limit);
}
