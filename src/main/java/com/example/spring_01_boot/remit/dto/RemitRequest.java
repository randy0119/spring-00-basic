package com.example.spring_01_boot.remit.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RemitRequest {
    @NotBlank(message = "요청자 ID는 필수에요.")
    private String requesterId;
    @NotBlank(message = "수신자 ID는 필수에요.")
    private String receiverId;
    @NotNull(message = "송금 요청 금액은 필수에요.")
    private Integer amount;
}
