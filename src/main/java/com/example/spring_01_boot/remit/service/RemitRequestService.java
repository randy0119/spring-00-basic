package com.example.spring_01_boot.remit.service;

import com.example.spring_01_boot.remit.dto.RemitRequestResponse;

import java.util.List;

public interface RemitRequestService {
    RemitRequestResponse createRemitRequest(String requesterId, String receiverId, Integer amount);
    List<RemitRequestResponse> getRemitRequestsByRequesterToReceiver(String requesterId, String receiverId);
}
