package com.example.spring_01_boot.point.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import com.example.spring_01_boot.point.service.PointService;
import com.example.spring_01_boot.point.dto.PointOperationResponse;
import lombok.RequiredArgsConstructor;
import com.example.spring_01_boot.point.controller.dto.pointRequest;

@RestController
@RequiredArgsConstructor
public class pointContoller {

    private final PointService pointService;
    
    @GetMapping("/point")
    public int getPoint(@RequestParam String userId) {
        return pointService.getPoint(userId);
    }

    @PostMapping("/point/charge")
    public PointOperationResponse chargePoint(@Valid @RequestBody pointRequest request) {
        return pointService.chargePoint(request.getUserId(), request.getAmount());
    }

    @PostMapping("/point/use")
    public PointOperationResponse usePoint(@Valid @RequestBody pointRequest request) {
        return pointService.usePoint(request.getUserId(), request.getAmount());
    }
}
