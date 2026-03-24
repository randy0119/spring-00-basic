package com.example.spring_01_boot.coupon.exception;

public enum CouponIssueConflictReason {
    /** 동일 사용자가 동일 쿠폰을 이미 발급받음 */
    DUPLICATE_USER_COUPON,
    /** 수량 소진 */
    COUPON_SOLD_OUT,
    /** 만료 */
    COUPON_EXPIRED
}
