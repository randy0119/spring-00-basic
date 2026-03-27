package com.example.spring_01_boot.remit.service;

import com.example.spring_01_boot.global.exception.ServiceException;
import com.example.spring_01_boot.remit.dto.RemitRequestResponse;
import com.example.spring_01_boot.remit.repository.RemitRequestRepository;
import com.example.spring_01_boot.remit.repository.entity.RemitRequest;
import com.example.spring_01_boot.remit.repository.entity.RemitRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemitRequestServiceImplTest {

    @Mock
    private RemitRequestRepository remitRequestRepository;

    @InjectMocks
    private RemitRequestServiceImpl remitRequestService;

    private RemitRequest mockSavedEntity;

    @BeforeEach
    void setUp() {
        mockSavedEntity = new RemitRequest("rmt_test1234", "testID-01", "testID-02", 10000);
        mockSavedEntity.setStatus(RemitRequestStatus.PENDING);
    }

    @Test
    @DisplayName("정상 송금 요청 생성")
    void createRemitRequest_success() {
        // given
        when(remitRequestRepository.save(any(RemitRequest.class))).thenReturn(mockSavedEntity);

        // when
        RemitRequestResponse response = remitRequestService.createRemitRequest("testID-01", "testID-02", 10000);

        // then
        assertThat(response.getRequesterId()).isEqualTo("testID-01");
        assertThat(response.getReceiverId()).isEqualTo("testID-02");
        assertThat(response.getAmount()).isEqualTo(10000);
        assertThat(response.getStatus()).isEqualTo(RemitRequestStatus.PENDING);
        assertThat(response.getRemitHashCode()).startsWith("rmt_");
    }

    @Test
    @DisplayName("본인에게 송금 요청 시 실패")
    void createRemitRequest_selfRemit_fail() {
        // when & then
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
        // when & then
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
        // when & then
        assertThatThrownBy(() ->
                remitRequestService.createRemitRequest("testID-01", "testID-02", 10000001)
        )
                .isInstanceOf(ServiceException.class)
                .hasMessage("1회 최대 송금 요청 금액은 10,000,000원을 넘길 수 없어요.")
                .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }
}