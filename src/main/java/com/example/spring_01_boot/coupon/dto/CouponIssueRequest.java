package com.example.spring_01_boot.coupon.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CouponIssueRequest {
    @NotBlank
    private String userId;
}
