package com.example.spring_01_boot.point.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.spring_01_boot.point.service.PointService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class pointContoller {

    private final PointService pointService;
    
    @GetMapping("/point")
    public int getPoint(@RequestParam String userId) {
        return pointService.getPoint(userId);
    }
}
