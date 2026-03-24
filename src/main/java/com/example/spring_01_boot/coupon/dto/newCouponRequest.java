package com.example.spring_01_boot.coupon.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.Instant;

@Data
public class newCouponRequest {
    @NotBlank
    private String name;

    @Positive
    private int totalQuantity;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant expiresAt;
}
