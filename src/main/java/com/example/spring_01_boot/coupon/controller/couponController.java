package com.example.spring_01_boot.coupon.controller;

import com.example.spring_01_boot.coupon.dto.CouponCreateResponse;
import com.example.spring_01_boot.coupon.dto.CouponIssueRequest;
import com.example.spring_01_boot.coupon.dto.CouponIssueResponse;
import com.example.spring_01_boot.coupon.dto.newCouponRequest;
import com.example.spring_01_boot.coupon.dto.CouponTransactionsResponse;
import com.example.spring_01_boot.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@Validated
@RequiredArgsConstructor
public class couponController {
    private final CouponService couponService;

    @PostMapping("/coupons")
    public CouponCreateResponse newCoupon(@Valid @RequestBody newCouponRequest request) {
        return couponService.newCoupon(request.getName(), request.getTotalQuantity(), request.getExpiresAt());
    }

    @PostMapping("/coupons/{couponId}/issue")
    public CouponIssueResponse issueCoupon(
        @PathVariable String couponId,
        @Valid @RequestBody CouponIssueRequest request
    ) {
        return couponService.issueCoupon(couponId, request.getUserId());
    }

    @GetMapping("/coupons/transactions")
    public CouponTransactionsResponse getTransactions(@RequestParam String userId, @RequestParam int limit) {
        return couponService.getTransactions(userId, limit);
    }
}
