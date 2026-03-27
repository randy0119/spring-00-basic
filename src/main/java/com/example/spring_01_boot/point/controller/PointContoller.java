package com.example.spring_01_boot.point.controller;

import com.example.spring_01_boot.point.dto.PointBalanceResponse;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.example.spring_01_boot.point.service.PointService;
import com.example.spring_01_boot.point.dto.PointOperationResponse;
import com.example.spring_01_boot.point.dto.PointTransactionsResponse;
import lombok.RequiredArgsConstructor;
import com.example.spring_01_boot.point.controller.dto.PointRequest;
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequiredArgsConstructor
public class PointContoller {

    private final PointService pointService;
    
    @GetMapping("/point")
    public PointBalanceResponse getPoint(@RequestParam String userId) {
        return pointService.getPoint(userId);
    }

    @GetMapping("/point/transactions")
    public PointTransactionsResponse getTransactions(
        @RequestParam String userId,
        @RequestParam(defaultValue = "20") @Min(value = 1, message = "조회 건수는 1 이상이어야 합니다.") @Max(value = 200, message = "한번에 조회 가능한 최대 거래내역 갯수는 200개입니다.") int limit) {
        return pointService.getTransactions(userId, limit);
    }

    @PostMapping("/point/charge")
    public PointOperationResponse chargePoint(@Valid @RequestBody PointRequest request) {
        return pointService.chargePoint(request.getUserId(), request.getAmount());
    }

    @PostMapping("/point/use")
    public PointOperationResponse usePoint(@Valid @RequestBody PointRequest request) {
        return pointService.usePoint(request.getUserId(), request.getAmount());
    }
}
