package com.example.spring_01_boot.point.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "point_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PointTransactionType transactionType;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private long afterBalance;

    /** 거래 시각 (UTC) */
    @Column(name = "txn_at", nullable = false)
    private Instant timestamp;

    public PointTransaction(String userId, PointTransactionType transactionType, long amount, long afterBalance, Instant timestamp) {
        this.userId = userId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.afterBalance = afterBalance;
        this.timestamp = timestamp;
    }
}
