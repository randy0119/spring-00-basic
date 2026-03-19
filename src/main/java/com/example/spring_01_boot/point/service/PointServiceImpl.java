package com.example.spring_01_boot.point.service;

import com.example.spring_01_boot.point.repository.PointRepository;
import com.example.spring_01_boot.point.repository.entity.Point;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    private final PointRepository pointRepository;
    @Override
    public int getPoint(String userId) {
        Point point = pointRepository.findById(userId)
            .orElseGet(() -> pointRepository.save(new Point(userId, 0L)));

        // balance는 long 이지만, 서비스 인터페이스가 int를 반환하도록 되어 있으므로 캐스팅
        return (int) point.getBalance();
    }
    /*@Override
    public void chargePoint(String userId, int amount) {
        return;
    }
    @Override
    public void usePoint(String userId, int amount) {
        return;
    }*/
}
