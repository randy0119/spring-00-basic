package com.example.spring_01_boot.remit.service;

import com.example.spring_01_boot.global.exception.ServiceException;
import com.example.spring_01_boot.remit.client.PointClient;
import com.example.spring_01_boot.remit.dto.RemitRequestResponse;
import com.example.spring_01_boot.remit.repository.RemitRequestRepository;
import com.example.spring_01_boot.remit.repository.entity.RemitRequest;
import com.example.spring_01_boot.remit.repository.entity.RemitRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemitRequestServiceImplTest {

    @Mock
    private RemitRequestRepository remitRequestRepository;

    @Mock
    private PointClient pointClient;

    @InjectMocks
    private RemitRequestServiceImpl remitRequestService;

    // ──────────────────────────────────────────
    // 송금 요청 생성
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("송금 요청 생성")
    class CreateRemitRequest {

        private RemitRequest mockSavedEntity;

        @BeforeEach
        void setUp() {
            mockSavedEntity = new RemitRequest("rmt_test1234", "testID-01", "testID-02", 10000);
            mockSavedEntity.setStatus(RemitRequestStatus.PENDING);
        }

        @Test
        @DisplayName("정상 송금 요청 생성")
        void createRemitRequest_success() {
            when(remitRequestRepository.save(any(RemitRequest.class))).thenReturn(mockSavedEntity);

            RemitRequestResponse response = remitRequestService.createRemitRequest("testID-01", "testID-02", 10000);

            assertThat(response.getRequesterId()).isEqualTo("testID-01");
            assertThat(response.getReceiverId()).isEqualTo("testID-02");
            assertThat(response.getAmount()).isEqualTo(10000);
            assertThat(response.getStatus()).isEqualTo(RemitRequestStatus.PENDING);
            assertThat(response.getRemitHashCode()).startsWith("rmt_");
        }

        @Test
        @DisplayName("본인에게 송금 요청 시 실패")
        void createRemitRequest_selfRemit_fail() {
            assertThatThrownBy(() ->
                    remitRequestService.createRemitRequest("testID-01", "testID-01", 10000)
            )
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("송금요청 대상이 본인이에요.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("요청 금액이 0 이하일 때 실패")
        void createRemitRequest_invalidAmount_zero_fail() {
            assertThatThrownBy(() ->
                    remitRequestService.createRemitRequest("testID-01", "testID-02", 0)
            )
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("요청 금액은 0보다 커야 해요.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("요청 금액이 최대 한도 초과 시 실패")
        void createRemitRequest_invalidAmount_overMax_fail() {
            assertThatThrownBy(() ->
                    remitRequestService.createRemitRequest("testID-01", "testID-02", 10000001)
            )
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("1회 최대 송금 요청 금액은 10,000,000원을 넘길 수 없어요.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ──────────────────────────────────────────
    // 송금 요청 목록 조회
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("송금 요청 목록 조회")
    class GetRemitRequests {

        private RemitRequest mockRequesterRequest;
        private RemitRequest mockReceiverRequest;

        @BeforeEach
        void setUp() {
            mockRequesterRequest = new RemitRequest("rmt_test0001", "testID-03", "testID-04", 10000);
            mockRequesterRequest.setStatus(RemitRequestStatus.PENDING);

            mockReceiverRequest = new RemitRequest("rmt_test0002", "testID-05", "testID-04", 20000);
            mockReceiverRequest.setStatus(RemitRequestStatus.PENDING);
        }

        @Test
        @DisplayName("요청자ID로 조회 성공")
        void getRemitRequests_byRequesterId_success() {
            when(remitRequestRepository.findAllByRequesterId("testID-03"))
                    .thenReturn(List.of(mockRequesterRequest));

            List<RemitRequestResponse> result =
                    remitRequestService.getRemitRequestsByRequesterToReceiver("testID-03", null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRequesterId()).isEqualTo("testID-03");
        }

        @Test
        @DisplayName("수신자ID로 조회 성공")
        void getRemitRequests_byReceiverId_success() {
            when(remitRequestRepository.findAllByReceiverId("testID-04"))
                    .thenReturn(List.of(mockRequesterRequest, mockReceiverRequest));

            List<RemitRequestResponse> result =
                    remitRequestService.getRemitRequestsByRequesterToReceiver(null, "testID-04");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getReceiverId()).isEqualTo("testID-04");
            assertThat(result.get(1).getReceiverId()).isEqualTo("testID-04");
        }

        @Test
        @DisplayName("요청자ID + 수신자ID 둘 다 입력 시 조회 성공")
        void getRemitRequests_byBoth_success() {
            when(remitRequestRepository.findAllByRequesterIdAndReceiverId("testID-03", "testID-04"))
                    .thenReturn(List.of(mockRequesterRequest));

            List<RemitRequestResponse> result =
                    remitRequestService.getRemitRequestsByRequesterToReceiver("testID-03", "testID-04");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRequesterId()).isEqualTo("testID-03");
            assertThat(result.get(0).getReceiverId()).isEqualTo("testID-04");
        }

        @Test
        @DisplayName("요청자ID와 수신자ID 둘 다 null일 때 실패 - 400")
        void getRemitRequests_bothNull_fail() {
            assertThatThrownBy(() ->
                    remitRequestService.getRemitRequestsByRequesterToReceiver(null, null)
            )
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("요청자ID 또는 수신자ID를 입력해주세요.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("요청자ID와 수신자ID가 동일할 때 실패 - 400")
        void getRemitRequests_sameId_fail() {
            assertThatThrownBy(() ->
                    remitRequestService.getRemitRequestsByRequesterToReceiver("testID-03", "testID-03")
            )
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("요청자ID와 수신자ID를 확인해주세요.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ──────────────────────────────────────────
    // 송금 요청 수락/거절
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("송금 요청 수락/거절")
    class DecisionRemitRequest {

        private RemitRequest mockPendingRequest;

        @BeforeEach
        void setUp() {
            mockPendingRequest = new RemitRequest("rmt_test9999", "testID-01", "testID-02", 10000);
            mockPendingRequest.setStatus(RemitRequestStatus.PENDING);
        }

        @Test
        @DisplayName("정상 수락 성공")
        void decisionRemitRequest_accept_success() {
            when(remitRequestRepository.findByRemitHashcodeAndReceiverId("rmt_test9999", "testID-02"))
                    .thenReturn(mockPendingRequest);
            when(pointClient.getBalance("testID-02")).thenReturn(20000);
            when(remitRequestRepository.save(any(RemitRequest.class))).thenReturn(mockPendingRequest);

            RemitRequestResponse response =
                    remitRequestService.decisionRemitRequest("rmt_test9999", "testID-02", true);

            assertThat(response.getStatus()).isEqualTo(RemitRequestStatus.ACCEPTED);
            verify(pointClient).use("testID-02", 10000);     // 수신자 차감
            verify(pointClient).charge("testID-01", 10000);  // 요청자 충전
        }

        @Test
        @DisplayName("정상 거절 성공")
        void decisionRemitRequest_reject_success() {
            when(remitRequestRepository.findByRemitHashcodeAndReceiverId("rmt_test9999", "testID-02"))
                    .thenReturn(mockPendingRequest);
            when(remitRequestRepository.save(any(RemitRequest.class))).thenReturn(mockPendingRequest);

            RemitRequestResponse response =
                    remitRequestService.decisionRemitRequest("rmt_test9999", "testID-02", false);

            assertThat(response.getStatus()).isEqualTo(RemitRequestStatus.REJECTED);
            verify(pointClient, never()).use(any(), anyInt());    // 포인트 변동 없음
            verify(pointClient, never()).charge(any(), anyInt()); // 포인트 변동 없음
        }

        @Test
        @DisplayName("존재하지 않는 송금 요청 - 400")
        void decisionRemitRequest_notFound_fail() {
            when(remitRequestRepository.findByRemitHashcodeAndReceiverId(any(), any()))
                    .thenReturn(null);

            assertThatThrownBy(() ->
                    remitRequestService.decisionRemitRequest("rmt_notexist", "testID-02", true)
            )
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("올바른 송금요청 해시번호와 수신자 ID인지 확인해주세요.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("만료된 송금 요청 수락 시 실패 - 410")
        void decisionRemitRequest_expired_fail() {
            RemitRequest expiredRequest = new RemitRequest("rmt_test9999", "testID-01", "testID-02", 10000);
            expiredRequest.setStatus(RemitRequestStatus.EXPIRED);
            when(remitRequestRepository.findByRemitHashcodeAndReceiverId("rmt_test9999", "testID-02"))
                    .thenReturn(expiredRequest);
            when(remitRequestRepository.save(any(RemitRequest.class))).thenReturn(expiredRequest);

            assertThatThrownBy(() ->
                    remitRequestService.decisionRemitRequest("rmt_test9999", "testID-02", true)
            )
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("이미 만료된 요청이에요.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.GONE));
        }

        @Test
        @DisplayName("수신자 잔고 부족 시 수락 실패 - 409")
        void decisionRemitRequest_insufficientBalance_fail() {
            when(remitRequestRepository.findByRemitHashcodeAndReceiverId("rmt_test9999", "testID-02"))
                    .thenReturn(mockPendingRequest);
            when(pointClient.getBalance("testID-02")).thenReturn(5000); // 잔고 부족

            assertThatThrownBy(() ->
                    remitRequestService.decisionRemitRequest("rmt_test9999", "testID-02", true)
            )
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("잔고를 확인해주세요.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));

            verify(pointClient, never()).use(any(), anyInt());    // 차감 안 됨
            verify(pointClient, never()).charge(any(), anyInt()); // 충전 안 됨
        }

        @Test
        @DisplayName("이미 수락된 요청에 다시 응답 시 실패 - 409")
        void decisionRemitRequest_alreadyAccepted_fail() {
            RemitRequest acceptedRequest = new RemitRequest("rmt_test9999", "testID-01", "testID-02", 10000);
            acceptedRequest.setStatus(RemitRequestStatus.ACCEPTED);
            when(remitRequestRepository.findByRemitHashcodeAndReceiverId("rmt_test9999", "testID-02"))
                    .thenReturn(acceptedRequest);

            assertThatThrownBy(() ->
                    remitRequestService.decisionRemitRequest("rmt_test9999", "testID-02", true)
            )
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("이미 처리된 요청이에요.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));
        }

        @Test
        @DisplayName("이미 거절된 요청에 다시 응답 시 실패 - 409")
        void decisionRemitRequest_alreadyRejected_fail() {
            RemitRequest rejectedRequest = new RemitRequest("rmt_test9999", "testID-01", "testID-02", 10000);
            rejectedRequest.setStatus(RemitRequestStatus.REJECTED);
            when(remitRequestRepository.findByRemitHashcodeAndReceiverId("rmt_test9999", "testID-02"))
                    .thenReturn(rejectedRequest);

            assertThatThrownBy(() ->
                    remitRequestService.decisionRemitRequest("rmt_test9999", "testID-02", false)
            )
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("이미 처리된 요청이에요.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));
        }
    }
}