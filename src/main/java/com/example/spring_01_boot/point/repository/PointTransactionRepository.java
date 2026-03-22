package com.example.spring_01_boot.point.repository;

import com.example.spring_01_boot.point.repository.entity.PointTransaction;
import com.example.spring_01_boot.point.repository.entity.PointTransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    List<PointTransaction> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    void deleteByUserId(String userId);

    long countByUserId(String userId);

    long countByUserIdAndTransactionType(String userId, PointTransactionType transactionType);
}
