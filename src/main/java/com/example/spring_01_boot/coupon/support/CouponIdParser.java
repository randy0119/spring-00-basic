package com.example.spring_01_boot.coupon.support;

import com.example.spring_01_boot.coupon.exception.InvalidCouponIdException;

import java.util.regex.Pattern;

/**
 * 외부 노출용 쿠폰 ID({@code c-123})와 DB PK(Long) 간 변환.
 */
public final class CouponIdParser {

    private static final Pattern PATTERN = Pattern.compile("^c-(\\d+)$", Pattern.CASE_INSENSITIVE);

    private CouponIdParser() {
    }

    public static long parseToDbId(String couponId) {
        if (couponId == null || couponId.isBlank()) {
            throw new InvalidCouponIdException("couponId is required");
        }
        var m = PATTERN.matcher(couponId.trim());
        if (!m.matches()) {
            throw new InvalidCouponIdException("Invalid couponId format. Expected: c-{numericId}");
        }
        return Long.parseLong(m.group(1));
    }

    public static String toExternalId(long dbId) {
        return "c-" + dbId;
    }
}
