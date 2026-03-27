package com.example.spring_01_boot.remit.client;

import com.example.spring_01_boot.global.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PointClient {

    private final RestTemplate restTemplate;

    @Value("${point.service.url:http://localhost:8080}")
    private String pointServiceUrl;

    // GET /point?userId={userId}
    public int getBalance(String userId) {
        try {
            String url = pointServiceUrl + "/point?userId=" + userId;
            PointBalanceResponse response = restTemplate.getForObject(url, PointBalanceResponse.class);
            return (int) response.getBalance();
        } catch (Exception e) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "잔액 조회에 실패했어요.");
        }
    }

    // POST /point/use
    public void use(String userId, int amount) {
        try {
            String url = pointServiceUrl + "/point/use";
            restTemplate.postForObject(url, Map.of("userId", userId, "amount", amount), Object.class);
        } catch (Exception e) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "포인트 차감에 실패했어요.");
        }
    }

    // POST /point/charge
    public void charge(String userId, int amount) {
        try {
            String url = pointServiceUrl + "/point/charge";
            restTemplate.postForObject(url, Map.of("userId", userId, "amount", amount), Object.class);
        } catch (Exception e) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR, "포인트 충전에 실패했어요.");
        }
    }

    // PointClient 내부 응답 DTO
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static class PointBalanceResponse {
        private long balance;
    }
}
