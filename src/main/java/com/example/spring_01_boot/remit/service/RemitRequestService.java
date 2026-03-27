package com.example.spring_01_boot.remit.service;

import com.example.spring_01_boot.remit.dto.RemitRequestResponse;
import com.example.spring_01_boot.remit.repository.entity.RemitRequestStatus;

public interface RemitRequestService {
    RemitRequestResponse createRemitRequest(String requestId, String receiverId, Integer amount);
}
