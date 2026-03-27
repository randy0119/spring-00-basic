package com.example.spring_01_boot.remit.service;

import com.example.spring_01_boot.global.exception.ServiceException;
import com.example.spring_01_boot.remit.client.PointClient;
import com.example.spring_01_boot.remit.dto.RemitRequestResponse;
import com.example.spring_01_boot.remit.repository.RemitRequestRepository;
import com.example.spring_01_boot.remit.repository.entity.RemitRequest;
import com.example.spring_01_boot.remit.repository.entity.RemitRequestStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RemitRequestServiceImpl implements RemitRequestService {
    private final RemitRequestRepository remitRequestRepository;
    private final PointClient pointClient;

    @Override
    @Transactional
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
        String remitHashcode = "rmt_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);

        // 4. 엔티티 생성
        RemitRequest remitRequest = new RemitRequest(remitHashcode, requesterId, receiverId, amount);

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
    @Transactional(readOnly = true)
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

    @Override
    @Transactional
    public RemitRequestResponse decisionRemitRequest(String remitHashcode, String receiverId, boolean isAccept) {
        // 1. 송금요청 해시코드 && 수신자 ID 확인
        RemitRequest remitRequest = remitRequestRepository.findByRemitHashcodeAndReceiverId(remitHashcode, receiverId);
        if (remitRequest == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "올바른 송금요청 해시번호와 수신자 ID인지 확인해주세요.");
        }

        // 2. 송금요청 완료여부 확인
        if (remitRequest.getStatus() == RemitRequestStatus.ACCEPTED || remitRequest.getStatus() == RemitRequestStatus.REJECTED) {
            throw new ServiceException(HttpStatus.CONFLICT, "이미 처리된 요청이에요.");
        }

        // 3. 송금요청 만료여부 확인
        if (remitRequest.getStatus() == RemitRequestStatus.EXPIRED || remitRequest.getExpiredAt().compareTo(Instant.now()) <= 0) {
            remitRequest.setStatus(RemitRequestStatus.EXPIRED);
            remitRequestRepository.save(remitRequest);
            throw new ServiceException(HttpStatus.GONE, "이미 만료된 요청이에요.");
        }

        RemitRequest saved;
        if (isAccept) {
            // Point API의 GET"/point?userId={id}"를 활용해 잔액조회해서 불가능하면 throw new ServiceException(HttpStatus.CONFLICT, "잔고를 확인해주세요");
            // 가능하면 Point API의 POST"/point/charge"를 활용해 먼저 수신자의 잔고를 차감하고, 그다음 POST"/point/use"활용해 요청자의 잔고를 늘린다.
            // 둘은 200코드로 둘 다 정상 동작했을때만 수행하는 트랜잭션이다.(물론 현재함수 전체가 트랜잭션이다.)
            String requesterId = remitRequest.getRequesterId();
            int amount = remitRequest.getAmount();

            // 4. 송금요청 수신자 잔액 확인 — GET /point?userId={requesterId}
            int receiverBalance = pointClient.getBalance(receiverId);
            if (receiverBalance <= amount) {
                throw new ServiceException(HttpStatus.CONFLICT, "잔고를 확인해주세요.");
            }

            // 5. 송금요청 수신자 잔액 차감 — POST /point/use
            pointClient.use(receiverId, amount);

            // 6. 요청자 잔액 충전 — POST /point/charge
            pointClient.charge(requesterId, amount);

            remitRequest.setStatus(RemitRequestStatus.ACCEPTED);
            remitRequest.setUpdatedAt(Instant.now());
            saved = remitRequestRepository.save(remitRequest);
        }
        else{
            remitRequest.setStatus(RemitRequestStatus.REJECTED);
            remitRequest.setUpdatedAt(Instant.now());
            saved = remitRequestRepository.save(remitRequest);
        }
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
}
