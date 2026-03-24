package com.example.spring_01_boot.coupon.service;

import com.example.spring_01_boot.coupon.dto.CouponCreateResponse;
import com.example.spring_01_boot.coupon.dto.CouponTransactionItemResponse;
import com.example.spring_01_boot.coupon.dto.CouponTransactionsResponse;
import com.example.spring_01_boot.coupon.dto.CouponIssueResponse;
import com.example.spring_01_boot.coupon.exception.CouponIssueConflictException;
import com.example.spring_01_boot.coupon.exception.CouponIssueConflictReason;
import com.example.spring_01_boot.coupon.exception.CouponNotFoundException;
import com.example.spring_01_boot.coupon.repository.CouponRepository;
import com.example.spring_01_boot.coupon.repository.CouponTransactionRepository;
import com.example.spring_01_boot.coupon.repository.entity.Coupon;
import com.example.spring_01_boot.coupon.repository.entity.CouponStatus;
import com.example.spring_01_boot.coupon.repository.entity.CouponTransaction;
import com.example.spring_01_boot.coupon.support.CouponIdParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;
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
            CouponIdParser.toExternalId(coupon.getId()),
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
    public CouponIssueResponse issueCoupon(String couponId, String userId) {
        long dbId = CouponIdParser.parseToDbId(couponId);
        Coupon coupon = couponRepository.findByIdForUpdate(dbId)
            .orElseThrow(() -> new CouponNotFoundException("coupon not found: " + couponId));

        Instant now = Instant.now();
        String couponName = coupon.getCouponName();

        if (couponTransactionRepository.existsByUserIdAndCouponName(userId, couponName)) {
            throw new CouponIssueConflictException(
                CouponIssueConflictReason.DUPLICATE_USER_COUPON,
                "user already issued this coupon"
            );
        }

        CouponStatus status = coupon.getStatus(now);
        if (status == CouponStatus.EXPIRED) {
            throw new CouponIssueConflictException(
                CouponIssueConflictReason.COUPON_EXPIRED,
                "coupon has expired"
            );
        }
        if (status == CouponStatus.SOLD_OUT) {
            throw new CouponIssueConflictException(
                CouponIssueConflictReason.COUPON_SOLD_OUT,
                "coupon is sold out"
            );
        }

        coupon.issue();
        CouponTransaction saved = couponTransactionRepository.save(
            new CouponTransaction(couponName, userId, now)
        );

        return new CouponIssueResponse(
            userId,
            CouponIdParser.toExternalId(coupon.getId()),
            "uc-" + saved.getId(),
            "ISSUED",
            now,
            coupon.getExpireDate()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CouponTransactionsResponse getTransactions(String userId, int limit) {
        int pageSize = Math.min(Math.max(limit, 1), 200);
        Pageable pageable = PageRequest.of(0, pageSize);
        List<CouponTransaction> rows =
            couponTransactionRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        List<CouponTransactionItemResponse> items = rows.stream()
            .map(tx -> new CouponTransactionItemResponse(
                tx.getId(),
                tx.getCouponName(),
                tx.getTimestamp()
            ))
            .toList();
        return new CouponTransactionsResponse(userId, items);
    }
}
