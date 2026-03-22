package com.example.spring_01_boot.point.service;

import com.example.spring_01_boot.point.dto.PointOperationResponse;

public interface PointService {
    int getPoint(String userId);
    /** 성공 시 처리 후 잔액 포함, 실패 시 메시지만 */
    PointOperationResponse chargePoint(String userId, int amount);
    /** 성공 시 처리 후 잔액 포함, 실패 시 메시지만 */
    PointOperationResponse usePoint(String userId, int amount);
    //List<PointTransaction> getPointTransactions(String userId, int limit);
}
