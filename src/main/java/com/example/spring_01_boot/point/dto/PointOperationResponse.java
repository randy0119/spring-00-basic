package com.example.spring_01_boot.point.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 포인트 충전/사용 결과. 성공 시 {@code balance}에 처리 후 잔액, 실패 시 {@code message}에 사유.
 */
@Getter
@AllArgsConstructor
public class PointOperationResponse {

    private String message;
    private long balance;
}
