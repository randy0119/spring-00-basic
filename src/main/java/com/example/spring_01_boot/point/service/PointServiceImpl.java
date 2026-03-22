package com.example.spring_01_boot.point.service;

import com.example.spring_01_boot.point.dto.PointOperationResponse;
import com.example.spring_01_boot.point.repository.PointRepository;
import com.example.spring_01_boot.point.repository.PointTransactionRepository;
import com.example.spring_01_boot.point.repository.entity.Point;
import com.example.spring_01_boot.point.repository.entity.PointTransaction;
import com.example.spring_01_boot.point.repository.entity.PointTransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Override
    public int getPoint(String userId) {
        Point point = pointRepository.findById(userId)
            .orElseGet(() -> pointRepository.save(new Point(userId, 0L)));
        return (int) point.getBalance();
    }

    @Override
    @Transactional
    public PointOperationResponse chargePoint(String userId, int amount) {
        if (amount <= 0) {
            return PointOperationResponse.fail("금액은 0보다 커야 합니다.");
        }
        try {
            Point point = pointRepository.findById(userId)
                .orElseGet(() -> pointRepository.save(new Point(userId, 0L)));
            point.charge(amount);
            pointRepository.save(point);
            Instant now = Instant.now();
            pointTransactionRepository.save(
                new PointTransaction(userId, PointTransactionType.CHARGE, amount, point.getBalance(), now));
            return PointOperationResponse.ok("충전이 완료되었습니다.", point.getBalance());
        } catch (IllegalArgumentException e) {
            return PointOperationResponse.fail(
                e.getMessage() != null ? e.getMessage() : "충전 처리 중 오류가 발생했습니다.");
        } catch (RuntimeException e) {
            return PointOperationResponse.fail(
                e.getMessage() != null ? e.getMessage() : "충전 처리 중 오류가 발생했습니다.");
        }
    }

    @Override
    @Transactional
    public PointOperationResponse usePoint(String userId, int amount) {
        if (amount <= 0) {
            return PointOperationResponse.fail("금액은 0보다 커야 합니다.");
        }
        try {
            Point point = pointRepository.findById(userId).orElse(null);
            if (point == null) {
                return PointOperationResponse.fail("잔액이 부족합니다.");
            }
            point.use(amount);
            pointRepository.save(point);
            Instant now = Instant.now();
            pointTransactionRepository.save(
                new PointTransaction(userId, PointTransactionType.USE, amount, point.getBalance(), now));
            return PointOperationResponse.ok("포인트 사용이 완료되었습니다.", point.getBalance());
        } catch (IllegalArgumentException e) {
            return PointOperationResponse.fail(
                e.getMessage() != null ? e.getMessage() : "사용 처리 중 오류가 발생했습니다.");
        } catch (IllegalStateException e) {
            return PointOperationResponse.fail(
                e.getMessage() != null ? e.getMessage() : "잔액이 부족합니다.");
        } catch (RuntimeException e) {
            return PointOperationResponse.fail(
                e.getMessage() != null ? e.getMessage() : "사용 처리 중 오류가 발생했습니다.");
        }
    }
}
