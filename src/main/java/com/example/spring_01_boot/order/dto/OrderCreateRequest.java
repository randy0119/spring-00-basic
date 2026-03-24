package com.example.spring_01_boot.order.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
public class OrderCreateRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String bascketId;
}
