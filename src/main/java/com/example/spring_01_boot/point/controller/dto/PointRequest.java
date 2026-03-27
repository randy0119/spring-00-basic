package com.example.spring_01_boot.point.controller.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Data
public class PointRequest {
    @NotBlank
    private String userId;
    @Positive(message = "금액은 0보다 커야 합니다.")
    private int amount;
}
