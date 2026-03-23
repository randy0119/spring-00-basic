package com.example.spring_01_boot.coupon.repository;

import com.example.spring_01_boot.coupon.repository.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCouponName(String couponName);
}