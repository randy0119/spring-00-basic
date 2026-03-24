package com.example.spring_01_boot.coupon.controller;

import com.example.spring_01_boot.coupon.controller.dto.newCouponRequest;
import com.example.spring_01_boot.coupon.dto.CouponCreateResponse;
import com.example.spring_01_boot.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@Validated
@RequiredArgsConstructor
public class couponController {
    private final CouponService couponService;

    @PostMapping("/coupons")
    public CouponCreateResponse newCoupon(@Valid @RequestBody newCouponRequest request) {
        return couponService.newCoupon(request.getName(), request.getTotalQuantity(), request.getExpiresAt());
    }

    @GetMapping("/coupons/issue")
    public void issueCoupon(@RequestParam String userId, @RequestParam String couponName) {
        couponService.issueCoupon(couponName, userId);
    }
}
