package com.example.spring_01_boot.coupon.repository;

import com.example.spring_01_boot.coupon.repository.entity.CouponTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponTransactionRepository extends JpaRepository<CouponTransaction, Long> {

    boolean existsByUserIdAndCouponName(String userId, String couponName);
}
