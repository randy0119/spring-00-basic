package com.example.spring_01_boot.remit.service;

import com.example.spring_01_boot.remit.dto.RemitRequestResponse;

public interface RemitRequestService {
    RemitRequestResponse createRemitRequest(String requesterId, String receiverId, Integer amount);
}
