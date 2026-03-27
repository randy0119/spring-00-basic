package com.example.spring_01_boot.remit.repository;

import com.example.spring_01_boot.remit.dto.RemitRequestResponse;
import com.example.spring_01_boot.remit.repository.entity.RemitRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RemitRequestRepository extends JpaRepository<RemitRequest, Long> {
    List<RemitRequest> findAllByRequesterId(String requesterId);
    List<RemitRequest> findAllByReceiverId(String receiverId);
    List<RemitRequest> findAllByRequesterIdAndReceiverId(String requesterId, String receiverId);
    RemitRequest findByRemitHashcodeAndReceiverId(String remitHashcode,  String receiverId);
}
