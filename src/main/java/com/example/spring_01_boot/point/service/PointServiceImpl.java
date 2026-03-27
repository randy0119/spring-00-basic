package com.example.spring_01_boot.point.service;

import com.example.spring_01_boot.global.exception.ServiceException;
import com.example.spring_01_boot.point.dto.PointBalanceResponse;
import com.example.spring_01_boot.point.dto.PointOperationResponse;
import com.example.spring_01_boot.point.dto.PointTransactionItemResponse;
import com.example.spring_01_boot.point.dto.PointTransactionsResponse;
import com.example.spring_01_boot.point.repository.PointRepository;
import com.example.spring_01_boot.point.repository.PointTransactionRepository;
import com.example.spring_01_boot.point.repository.entity.Point;
import com.example.spring_01_boot.point.repository.entity.PointTransaction;
import com.example.spring_01_boot.point.repository.entity.PointTransactionType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final PointRepository pointRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Override
    public PointBalanceResponse getPoint(String userId) {
        Point point = pointRepository.findById(userId)
            .orElseGet(() -> pointRepository.save(new Point(userId, 0L)));
        return new PointBalanceResponse(point.getBalance());
    }

    @Override
    @Transactional
    public PointOperationResponse chargePoint(String userId, int amount) {
        if (amount <= 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "금액은 0보다 커야 합니다.");
        }
        Point point = pointRepository.findById(userId)
                .orElseGet(() -> pointRepository.save(new Point(userId, 0L)));
        point.charge(amount);
        pointRepository.save(point);
        Instant now = Instant.now();
        pointTransactionRepository.save(
                new PointTransaction(userId, PointTransactionType.CHARGE, amount, point.getBalance(), now));
        return new PointOperationResponse("충전이 완료되었습니다.", point.getBalance());
    }

    @Override
    @Transactional
    public PointOperationResponse usePoint(String userId, int amount) {
        if (amount <= 0) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "금액은 0보다 커야 합니다.");
        }
        Point point = pointRepository.findById(userId).orElse(null);
        if (point == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자 입니다.");
        }
        point.use(amount);
        pointRepository.save(point);
        Instant now = Instant.now();
        pointTransactionRepository.save(
                new PointTransaction(userId, PointTransactionType.USE, amount, point.getBalance(), now));
        return new PointOperationResponse("포인트 사용이 완료되었습니다.", point.getBalance());
    }

    @Override
    @Transactional(readOnly = true)
    public PointTransactionsResponse getTransactions(String userId, int limit) {
        int pageSize = Math.min(Math.max(limit, 1), 200);
        Pageable pageable = PageRequest.of(0, pageSize);
        List<PointTransaction> rows =
            pointTransactionRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
        List<PointTransactionItemResponse> items = rows.stream()
            .map(tx -> new PointTransactionItemResponse(
                tx.getId(),
                tx.getTransactionType().name(),
                tx.getAmount(),
                tx.getAfterBalance(),
                tx.getTimestamp()))
            .toList();
        return new PointTransactionsResponse(userId, items);
    }
}
