package com.example.spring_01_boot.remit.dto;

import com.example.spring_01_boot.remit.repository.entity.RemitRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class RemitRequestResponse {
    private String remitHashCode;
    private String requesterId;
    private String receiverId;
    private int amount;
    private RemitRequestStatus status;
    private Instant createdAt;
    private Instant expiredAt;
    private String message;

}
