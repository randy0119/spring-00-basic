package com.example.spring_01_boot.coupon.repository;

import com.example.spring_01_boot.coupon.repository.entity.CouponTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CouponTransactionRepository extends JpaRepository<CouponTransaction, Long> {

    boolean existsByUserIdAndCouponName(String userId, String couponName);

    List<CouponTransaction> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);
}
