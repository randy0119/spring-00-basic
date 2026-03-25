package com.example.spring_01_boot.order.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import com.example.spring_01_boot.order.service.PayService;
import com.example.spring_01_boot.order.dto.PayResponse;
import com.example.spring_01_boot.order.dto.PayRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import com.example.spring_01_boot.order.repository.entity.PaymentType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;


@RestController
@Validated
@RequiredArgsConstructor
public class PayController {
    private final PayService payService;

    @PostMapping("/pay")
    public PayResponse pay(@Valid @RequestBody PayRequest request) {
        PaymentType paymentType;
        try {
            paymentType = PaymentType.valueOf(request.getPaymentType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("결제 종류가 올바르지 않습니다.");
        }
        return payService.pay(request.getOrderId(), paymentType, request.getCreditMethod(), request.getAmount());
    }

    @GetMapping("/pay/cancel")
    public PayResponse cancel(@RequestParam Long orderId) {
        return payService.cancel(orderId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of(
                "errorCode", "BAD_REQUEST",
                "message", e.getMessage()
            )
        );
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<Map<String, String>> handleValidationException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            Map.of(
                "errorCode", "VALIDATION_ERROR",
                "message", "요청 값이 올바르지 않습니다."
            )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            Map.of(
                "errorCode", "INTERNAL_SERVER_ERROR",
                "message", "서버 처리 중 오류가 발생했습니다."
            )
        );
    }
}
