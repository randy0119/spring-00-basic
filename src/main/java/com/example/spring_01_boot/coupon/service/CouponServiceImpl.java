package com.example.spring_01_boot.coupon.service;

import com.example.spring_01_boot.coupon.dto.CouponCreateResponse;
import com.example.spring_01_boot.coupon.repository.CouponRepository;
import com.example.spring_01_boot.coupon.repository.entity.Coupon;
import com.example.spring_01_boot.coupon.repository.entity.CouponStatus;
import com.example.spring_01_boot.coupon.repository.entity.CouponTransaction;
import com.example.spring_01_boot.coupon.repository.CouponTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponTransactionRepository couponTransactionRepository;

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

    @Override
    @Transactional
    public void issueCoupon(String couponName, String userId) {
        Coupon coupon = couponRepository.findByCouponName(couponName)
            .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
        try {
            coupon.issue();
            couponTransactionRepository.save(
                new CouponTransaction(couponName, userId, Instant.now())
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("쿠폰을 발급할 수 없습니다.");
        }
    }
}