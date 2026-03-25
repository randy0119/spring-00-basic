package com.example.spring_01_boot.order.repository.entity;

public enum PayStatus {
    PAID,     // 정상 결제 완료
    FAILED,   // 결제 실패
    CANCELED  // 결제 취소
}
