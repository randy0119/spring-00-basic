package com.example.spring_01_boot.remit.service;

import com.example.spring_01_boot.global.exception.ServiceException;
import com.example.spring_01_boot.remit.dto.RemitRequestResponse;
import com.example.spring_01_boot.remit.repository.RemitRequestRepository;
import com.example.spring_01_boot.remit.repository.entity.RemitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RemitRequestServiceImpl implements RemitRequestService {
    private final RemitRequestRepository remitRequestRepository;

    @Override
    public RemitRequestResponse createRemitRequest(String requesterId, String receiverId, Integer amount){
        // 1. 본인에게 요청 제한
        if (requesterId.equals(receiverId)){
            throw new ServiceException(HttpStatus.BAD_REQUEST, "송금요청 대상이 본인이에요.");
        }

        // 2-1. 최소 금액 검사
        if (amount <= 0){
            throw new ServiceException(HttpStatus.BAD_REQUEST, "요청 금액은 0보다 커야 해요.");
        }

        // 2-2. 최대 금액 검사
        if (amount > 10000000){
            throw new ServiceException(HttpStatus.BAD_REQUEST,"1회 최대 송금 요청 금액은 10,000,000원을 넘길 수 없어요.");
        }

        // 3. HashCode 생성
        String remitHashCode = "rmt_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);

        // 4. 엔티티 생성
        RemitRequest remitRequest = new RemitRequest(remitHashCode, requesterId, receiverId, amount);

        // 5. 저장
        RemitRequest saved =  this.remitRequestRepository.save(remitRequest);

        // 6. DTO 변환 후 반환
        return new RemitRequestResponse(
                saved.getRemitHashcode(),
                saved.getRequesterId(),
                saved.getReceiverId(),
                saved.getAmount(),
                saved.getStatus(),
                saved.getCreatedAt(),
                saved.getExpiredAt(),
                saved.getStatus().getMessage()
        );
    }

    @Override
    public List<RemitRequestResponse> getRemitRequestsByRequesterToReceiver(String requesterId, String receiverId) {
        // 둘 다 없을 때
        if (requesterId == null && receiverId == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "요청자ID 또는 수신자ID를 입력해주세요.");
        }

        // 동일인
        if (requesterId != null && requesterId.equals(receiverId)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "요청자ID와 수신자ID를 확인해주세요.");
        }

        List<RemitRequest> remitRequests;

        if (requesterId != null && receiverId != null) {
            // 둘 다 있을 때
            remitRequests = remitRequestRepository.findAllByRequesterIdAndReceiverId(requesterId, receiverId);
        } else if (requesterId != null) {
            // 요청자만 있을 때
            remitRequests = remitRequestRepository.findAllByRequesterId(requesterId);
        } else {
            // 수신자만 있을 때
            remitRequests = remitRequestRepository.findAllByReceiverId(receiverId);
        }

        return remitRequests.stream().map(
                remitRequest -> new RemitRequestResponse(
                        remitRequest.getRemitHashcode(),
                        remitRequest.getRequesterId(),
                        remitRequest.getReceiverId(),
                        remitRequest.getAmount(),
                        remitRequest.getStatus(),
                        remitRequest.getCreatedAt(),
                        remitRequest.getExpiredAt(),
                        remitRequest.getStatus().getMessage()
                )
        ).toList();
    }
}
