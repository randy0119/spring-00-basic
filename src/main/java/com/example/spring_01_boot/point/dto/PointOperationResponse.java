package com.example.spring_01_boot.point.dto;

/**
 * 포인트 충전/사용 결과. 성공 시 {@code balance}에 처리 후 잔액, 실패 시 {@code message}에 사유.
 */
public record PointOperationResponse(boolean success, String message, Long balance) {

    public static PointOperationResponse ok(String message, long balance) {
        return new PointOperationResponse(true, message, balance);
    }

    public static PointOperationResponse fail(String message) {
        return new PointOperationResponse(false, message, null);
    }
}
