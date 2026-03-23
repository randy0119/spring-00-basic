package com.example.spring_01_boot.coupon.service;

import com.example.spring_01_boot.coupon.dto.CouponCreateResponse;
import com.example.spring_01_boot.coupon.repository.CouponRepository;
import com.example.spring_01_boot.coupon.repository.entity.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    public CouponCreateResponse newCoupon(String name, int totalQuantity, Instant expiresAt) {
        Instant now = Instant.now();

        Coupon coupon = couponRepository.findByCouponName(name)
            .orElseGet(() ->
                couponRepository.save(
                    new Coupon(name, totalQuantity, 0, expiresAt, now)
                )
            );

        return new CouponCreateResponse(
            "c-" + coupon.getId(),
            coupon.getCouponName(),
            coupon.getTotalQuantity(),
            coupon.getIssuedQuantity(),
            coupon.getExpireDate(),
            coupon.getStatus(now),
            coupon.getCreatedAt()
        );
    }
}