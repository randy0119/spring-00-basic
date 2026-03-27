package com.example.spring_01_boot.point.repository.entity;

import com.example.spring_01_boot.global.exception.ServiceException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.springframework.http.HttpStatus;

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

    /** 충전: amount는 양수여야 한다. */
    public void charge(long amount) {
        if (amount <= 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "금액은 0보다 커야 합니다.");
        }
        this.balance += amount;
    }

    /** 사용: amount는 양수이고 잔액 이하여야 한다. */
    public void use(long amount) {
        if (amount <= 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "금액은 0보다 커야 합니다.");
        }
        if (this.balance < amount) {
            throw new ServiceException(HttpStatus.CONFLICT, "잔액이 부족합니다.");
        }
        this.balance -= amount;
    }
}