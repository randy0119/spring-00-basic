package com.example.spring_01_boot.coupon.service;

import com.example.spring_01_boot.coupon.dto.CouponCreateResponse;
import com.example.spring_01_boot.coupon.dto.CouponIssueResponse;

import java.time.Instant;

public interface CouponService {
    CouponCreateResponse newCoupon(String name, int totalQuantity, Instant expiresAt);

    CouponIssueResponse issueCoupon(String couponId, String userId);
}
