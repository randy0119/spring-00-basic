package com.example.spring_01_boot.point.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point {
    @Id
    private String userId;
    private long balance = 0L;

    public Point(String userId, long balance) {
        this.userId = userId;
        this.balance = balance;
    }
}