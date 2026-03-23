package com.example.spring_01_boot.coupon.service;

import com.example.spring_01_boot.coupon.dto.CouponCreateResponse;
import java.time.Instant;

public interface CouponService {
    CouponCreateResponse newCoupon(String name, int totalQuantity, Instant expiresAt);
}
