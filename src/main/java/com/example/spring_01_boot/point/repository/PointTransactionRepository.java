package com.example.spring_01_boot.point.repository;

import com.example.spring_01_boot.point.repository.entity.PointTransaction;
import com.example.spring_01_boot.point.repository.entity.PointTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    void deleteByUserId(String userId);

    long countByUserId(String userId);

    long countByUserIdAndTransactionType(String userId, PointTransactionType transactionType);
}
