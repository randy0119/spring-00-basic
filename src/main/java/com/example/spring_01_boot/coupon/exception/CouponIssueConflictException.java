package com.example.spring_01_boot.coupon.exception;

import lombok.Getter;

@Getter
public class CouponIssueConflictException extends RuntimeException {

    private final CouponIssueConflictReason reason;
    private final String errorCode;

    public CouponIssueConflictException(CouponIssueConflictReason reason, String message) {
        super(message);
        this.reason = reason;
        this.errorCode = switch (reason) {
            case DUPLICATE_USER_COUPON -> "COUPON_ALREADY_ISSUED";
            case COUPON_SOLD_OUT -> "COUPON_SOLD_OUT";
            case COUPON_EXPIRED -> "COUPON_EXPIRED";
        };
    }
}
