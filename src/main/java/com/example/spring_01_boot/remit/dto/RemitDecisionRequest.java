package com.example.spring_01_boot.remit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RemitDecisionRequest {
    @NotBlank(message = "송금요청 해시코드를 입력해주세요.")
    private String remitHashcode;
    @NotBlank(message = "수신자 ID를 입력해주세요.")
    private String receiverId;
    @NotNull(message = "송금 요청에 동의하시나요?")
    private Decision decision;
}
