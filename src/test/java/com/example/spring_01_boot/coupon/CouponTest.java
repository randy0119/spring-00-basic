package com.example.spring_01_boot.coupon;

import com.example.spring_01_boot.coupon.dto.CouponCreateResponse;
import com.example.spring_01_boot.coupon.dto.CouponIssueResponse;
import com.example.spring_01_boot.coupon.dto.CouponTransactionItemResponse;
import com.example.spring_01_boot.coupon.dto.CouponTransactionsResponse;
import com.example.spring_01_boot.coupon.exception.CouponIssueConflictException;
import com.example.spring_01_boot.coupon.exception.CouponIssueConflictReason;
import com.example.spring_01_boot.coupon.exception.CouponNotFoundException;
import com.example.spring_01_boot.coupon.exception.InvalidCouponIdException;
import com.example.spring_01_boot.coupon.repository.CouponRepository;
import com.example.spring_01_boot.coupon.repository.CouponTransactionRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class CouponTest {

    private static final String COUPON_A = "WELCOME-5000";
    private static final String COUPON_B = "FLASH-1000";
    private static final String COUPON_ISSUE = "ISSUE-TEST";
    private static final String USER_1 = "u-1001";
    private static final String USER_NO_COUPONS = "u-empty";

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponTransactionRepository couponTransactionRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            couponTransactionRepository.deleteAll();
            couponRepository.deleteAll();
        });
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

    @Test
    void issueCoupon_success_incrementsIssuedQuantityAndReturnsResponse() {
        Instant expiresAt = Instant.parse("2026-12-31T23:59:59Z");
        CouponCreateResponse created = couponService.newCoupon(COUPON_ISSUE, 10, expiresAt);

        CouponIssueResponse issued = couponService.issueCoupon(created.couponId(), USER_1);

        assertThat(issued.userId()).isEqualTo(USER_1);
        assertThat(issued.couponId()).isEqualTo(created.couponId());
        assertThat(issued.userCouponId()).startsWith("uc-");
        assertThat(issued.status()).isEqualTo("ISSUED");
        assertThat(issued.expiresAt()).isEqualTo(expiresAt);
        assertThat(issued.issuedAt()).isNotNull();

        Coupon coupon = couponRepository.findByCouponName(COUPON_ISSUE).orElseThrow();
        assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
        assertThat(couponTransactionRepository.count()).isEqualTo(1);
    }

    @Test
    void issueCoupon_wrongCouponId_throwsNotFound() {
        assertThatThrownBy(() -> couponService.issueCoupon("c-999999", USER_1))
            .isInstanceOf(CouponNotFoundException.class)
            .hasMessageContaining("not found");
    }

    @Test
    void issueCoupon_invalidCouponIdFormat_throws() {
        assertThatThrownBy(() -> couponService.issueCoupon("not-a-coupon-id", USER_1))
            .isInstanceOf(InvalidCouponIdException.class);
    }

    @Test
    void issueCoupon_whenQuantityExceeded_throwsAndDoesNotPersistExtraTransaction() {
        Instant expiresAt = Instant.parse("2026-12-31T23:59:59Z");
        CouponCreateResponse created = couponService.newCoupon(COUPON_ISSUE, 1, expiresAt);

        couponService.issueCoupon(created.couponId(), USER_1);

        assertThatThrownBy(() -> couponService.issueCoupon(created.couponId(), USER_1))
            .isInstanceOf(CouponIssueConflictException.class)
            .satisfies(ex -> {
                CouponIssueConflictException e = (CouponIssueConflictException) ex;
                assertThat(e.getReason()).isEqualTo(CouponIssueConflictReason.DUPLICATE_USER_COUPON);
                assertThat(e.getErrorCode()).isEqualTo("COUPON_ALREADY_ISSUED");
            });

        Coupon coupon = couponRepository.findByCouponName(COUPON_ISSUE).orElseThrow();
        assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
        assertThat(couponTransactionRepository.count()).isEqualTo(1);
    }

    @Test
    void issueCoupon_whenExpired_throwsConflict() {
        Instant past = Instant.now().minusSeconds(120);
        CouponCreateResponse created = couponService.newCoupon(COUPON_ISSUE, 10, past);

        assertThatThrownBy(() -> couponService.issueCoupon(created.couponId(), USER_1))
            .isInstanceOf(CouponIssueConflictException.class)
            .satisfies(ex -> {
                CouponIssueConflictException e = (CouponIssueConflictException) ex;
                assertThat(e.getReason()).isEqualTo(CouponIssueConflictReason.COUPON_EXPIRED);
            });
        assertThat(couponTransactionRepository.count()).isZero();
    }

    @Test
    void issueCoupon_whenSoldOut_secondUserGetsSoldOut() {
        Instant expiresAt = Instant.parse("2026-12-31T23:59:59Z");
        CouponCreateResponse created = couponService.newCoupon(COUPON_ISSUE, 1, expiresAt);

        couponService.issueCoupon(created.couponId(), "user-a");

        assertThatThrownBy(() -> couponService.issueCoupon(created.couponId(), "user-b"))
            .isInstanceOf(CouponIssueConflictException.class)
            .satisfies(ex -> {
                CouponIssueConflictException e = (CouponIssueConflictException) ex;
                assertThat(e.getReason()).isEqualTo(CouponIssueConflictReason.COUPON_SOLD_OUT);
            });

        assertThat(couponRepository.findByCouponName(COUPON_ISSUE).orElseThrow().getIssuedQuantity()).isEqualTo(1);
        assertThat(couponTransactionRepository.count()).isEqualTo(1);
    }

    @Test
    void getTransactions_whenUserHasNoIssues_returnsEmptyList() {
        CouponTransactionsResponse response = couponService.getTransactions(USER_NO_COUPONS, 20);

        assertThat(response.userId()).isEqualTo(USER_NO_COUPONS);
        assertThat(response.transactions()).isEmpty();
    }

    @Test
    void getTransactions_afterSingleIssue_returnsOneItem() {
        Instant expiresAt = Instant.parse("2026-12-31T23:59:59Z");
        CouponCreateResponse created = couponService.newCoupon(COUPON_ISSUE, 5, expiresAt);
        CouponIssueResponse issued = couponService.issueCoupon(created.couponId(), USER_1);

        CouponTransactionsResponse response = couponService.getTransactions(USER_1, 20);

        assertThat(response.userId()).isEqualTo(USER_1);
        assertThat(response.transactions()).hasSize(1);
        CouponTransactionItemResponse item = response.transactions().get(0);
        assertThat(item.transactionId()).isEqualTo(Long.parseLong(issued.userCouponId().substring(3)));
        assertThat(item.couponName()).isEqualTo(COUPON_ISSUE);
        assertThat(item.timestamp()).isEqualTo(issued.issuedAt());
    }

    @Test
    void getTransactions_multipleCoupons_newestFirst() throws Exception {
        Instant expiresAt = Instant.parse("2026-12-31T23:59:59Z");
        CouponCreateResponse first = couponService.newCoupon("COUPON-OLDER", 5, expiresAt);
        Thread.sleep(5);
        CouponCreateResponse second = couponService.newCoupon("COUPON-NEWER", 5, expiresAt);

        couponService.issueCoupon(first.couponId(), USER_1);
        Thread.sleep(5);
        couponService.issueCoupon(second.couponId(), USER_1);

        CouponTransactionsResponse response = couponService.getTransactions(USER_1, 20);

        assertThat(response.transactions()).hasSize(2);
        assertThat(response.transactions().get(0).couponName()).isEqualTo("COUPON-NEWER");
        assertThat(response.transactions().get(1).couponName()).isEqualTo("COUPON-OLDER");
        assertThat(response.transactions().get(0).timestamp())
            .isAfterOrEqualTo(response.transactions().get(1).timestamp());
    }

    @Test
    void getTransactions_respectsLimit() {
        Instant expiresAt = Instant.parse("2026-12-31T23:59:59Z");
        for (int i = 0; i < 3; i++) {
            CouponCreateResponse c = couponService.newCoupon("LIMIT-C-" + i, 2, expiresAt);
            couponService.issueCoupon(c.couponId(), USER_1);
        }

        CouponTransactionsResponse response = couponService.getTransactions(USER_1, 2);

        assertThat(response.transactions()).hasSize(2);
    }
}
