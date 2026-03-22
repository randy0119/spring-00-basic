package com.example.spring_01_boot.point.controller.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Data
public class pointRequest {
    @NotBlank
    private String userId;
    @Positive
    private int amount;
}
