package com.example.spring_01_boot.remit.repository.entity;

public enum RemitRequestStatus {
    PENDING("정상적으로 송금 요청했어요."),
    ACCEPTED("송금이 완료됐어요."),
    REJECTED("송금 요청이 거절됐어요."),
    EXPIRED("송금 요청이 만료됐어요.");

    private final String message;

    RemitRequestStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
