package com.example.spring_01_boot.coupon;

import com.example.spring_01_boot.coupon.dto.CouponCreateResponse;
import com.example.spring_01_boot.coupon.repository.CouponRepository;
import com.example.spring_01_boot.coupon.repository.entity.Coupon;
import com.example.spring_01_boot.coupon.repository.entity.CouponStatus;
import com.example.spring_01_boot.coupon.service.CouponService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CouponTest {

    private static final String COUPON_A = "WELCOME-5000";
    private static final String COUPON_B = "FLASH-1000";

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> couponRepository.deleteAll());
    }

    @Test
    void newCoupon_success_createsCouponAndReturnsAvailable() {
        Instant expiresAt = Instant.parse("2026-12-31T23:59:59Z");

        CouponCreateResponse response = couponService.newCoupon(COUPON_A, 100, expiresAt);

        assertThat(response.couponId()).startsWith("c-");
        assertThat(response.name()).isEqualTo(COUPON_A);
        assertThat(response.totalQuantity()).isEqualTo(100);
        assertThat(response.issuedQuantity()).isZero();
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
        assertThat(response.status()).isEqualTo(CouponStatus.AVAILABLE);
        assertThat(response.createdAt()).isNotNull();

        assertThat(couponRepository.count()).isEqualTo(1);
        Coupon coupon = couponRepository.findByCouponName(COUPON_A).orElseThrow();
        assertThat(coupon.getTotalQuantity()).isEqualTo(100);
        assertThat(coupon.getIssuedQuantity()).isZero();
        assertThat(coupon.getExpireDate()).isEqualTo(expiresAt);
    }

    @Test
    void newCoupon_whenNameDuplicated_returnsExistingCouponWithoutCreatingNewRow() {
        Instant firstExpiresAt = Instant.parse("2026-12-31T23:59:59Z");
        Instant secondExpiresAt = Instant.parse("2027-12-31T23:59:59Z");

        CouponCreateResponse first = couponService.newCoupon(COUPON_B, 50, firstExpiresAt);
        CouponCreateResponse second = couponService.newCoupon(COUPON_B, 999, secondExpiresAt);

        assertThat(couponRepository.count()).isEqualTo(1);
        assertThat(second.couponId()).isEqualTo(first.couponId());
        assertThat(second.totalQuantity()).isEqualTo(50);
        assertThat(second.expiresAt()).isEqualTo(firstExpiresAt);
    }

    @Test
    void newCoupon_whenExpiredAtCreation_returnsExpiredStatus() {
        Instant pastExpiresAt = Instant.now().minusSeconds(60);

        CouponCreateResponse response = couponService.newCoupon(COUPON_A, 10, pastExpiresAt);

        assertThat(response.status()).isEqualTo(CouponStatus.EXPIRED);
    }
}
