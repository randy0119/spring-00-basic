package com.example.spring_01_boot.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import com.example.spring_01_boot.coupon.repository.entity.CouponTransaction;

public interface CouponTransactionRepository extends JpaRepository<CouponTransaction, Long> {
    //List<CouponTransaction> findByCouponNameOrderByTimestampDesc(String couponName, Pageable pageable);

    //void deleteByCouponName(String couponName);

    //long countByCouponName(String couponName);
}
