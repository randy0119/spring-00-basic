package com.example.spring_01_boot.point.service;

import com.example.spring_01_boot.global.exception.ServiceException;
import com.example.spring_01_boot.point.dto.PointOperationResponse;
import com.example.spring_01_boot.point.dto.PointTransactionsResponse;
import com.example.spring_01_boot.point.repository.PointRepository;
import com.example.spring_01_boot.point.repository.PointTransactionRepository;
import com.example.spring_01_boot.point.repository.entity.Point;
import com.example.spring_01_boot.point.repository.entity.PointTransaction;
import com.example.spring_01_boot.point.repository.entity.PointTransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceImplUnitTest {

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private PointServiceImpl pointService;

    // ──────────────────────────────────────────
    // 잔액 조회
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("잔액 조회")
    class GetPoint {

        @Test
        @DisplayName("유저 행이 있으면 잔액 반환")
        void getPoint_existingUser_returnsBalance() {
            when(pointRepository.findById("user-01"))
                    .thenReturn(Optional.of(new Point("user-01", 500L)));

            long balance = pointService.getPoint("user-01").getBalance();

            assertThat(balance).isEqualTo(500L);
        }

        @Test
        @DisplayName("유저 행이 없으면 0으로 생성 후 반환")
        void getPoint_newUser_createsRowAndReturnsZero() {
            when(pointRepository.findById("user-01")).thenReturn(Optional.empty());
            when(pointRepository.save(any(Point.class))).thenReturn(new Point("user-01", 0L));

            long balance = pointService.getPoint("user-01").getBalance();

            assertThat(balance).isEqualTo(0L);
            verify(pointRepository).save(any(Point.class));
        }
    }

    // ──────────────────────────────────────────
    // 포인트 충전
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("포인트 충전")
    class ChargePoint {

        @Test
        @DisplayName("정상 충전 성공")
        void chargePoint_success() {
            Point point = new Point("user-01", 0L);
            when(pointRepository.findById("user-01")).thenReturn(Optional.of(point));
            when(pointRepository.save(any(Point.class))).thenReturn(point);
            when(pointTransactionRepository.save(any(PointTransaction.class)))
                    .thenReturn(any(PointTransaction.class));

            PointOperationResponse response = pointService.chargePoint("user-01", 100);

            assertThat(response.getMessage()).isEqualTo("충전이 완료되었습니다.");
            assertThat(response.getBalance()).isEqualTo(100L);
        }

        @Test
        @DisplayName("신규 유저 충전 시 행 생성 후 충전")
        void chargePoint_newUser_createsRowAndCharges() {
            Point newPoint = new Point("user-01", 0L);
            when(pointRepository.findById("user-01")).thenReturn(Optional.empty());
            when(pointRepository.save(any(Point.class))).thenReturn(newPoint);

            PointOperationResponse response = pointService.chargePoint("user-01", 100);

            assertThat(response.getMessage()).isEqualTo("충전이 완료되었습니다.");
            verify(pointRepository, times(2)).save(any(Point.class)); // 생성 + 충전 후 저장
        }

        @Test
        @DisplayName("금액이 0일 때 실패 - 400")
        void chargePoint_zeroAmount_fail() {
            assertThatThrownBy(() -> pointService.chargePoint("user-01", 0))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("금액은 0보다 커야 합니다.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("금액이 음수일 때 실패 - 400")
        void chargePoint_negativeAmount_fail() {
            assertThatThrownBy(() -> pointService.chargePoint("user-01", -1))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("금액은 0보다 커야 합니다.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }
    }

    // ──────────────────────────────────────────
    // 포인트 사용
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("포인트 사용")
    class UsePoint {

        @Test
        @DisplayName("정상 사용 성공")
        void usePoint_success() {
            Point point = new Point("user-01", 200L);
            when(pointRepository.findById("user-01")).thenReturn(Optional.of(point));
            when(pointRepository.save(any(Point.class))).thenReturn(point);

            PointOperationResponse response = pointService.usePoint("user-01", 50);

            assertThat(response.getMessage()).isEqualTo("포인트 사용이 완료되었습니다.");
            assertThat(response.getBalance()).isEqualTo(150L);
        }

        @Test
        @DisplayName("잔액 부족 시 실패 - 409")
        void usePoint_insufficientBalance_fail() {
            Point point = new Point("user-01", 10L);
            when(pointRepository.findById("user-01")).thenReturn(Optional.of(point));

            // Point.use()에서 잔액 부족 시 ServiceException을 던진다고 가정
            assertThatThrownBy(() -> pointService.usePoint("user-01", 100))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("잔액이 부족합니다.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));
        }

        @Test
        @DisplayName("유저 행 없을 때 실패 - 400")
        void usePoint_noUser_fail() {
            when(pointRepository.findById("user-01")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pointService.usePoint("user-01", 50))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("존재하지 않는 사용자 입니다.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("금액이 0일 때 실패 - 400")
        void usePoint_zeroAmount_fail() {
            assertThatThrownBy(() -> pointService.usePoint("user-01", 0))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("금액은 0보다 커야 합니다.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("금액이 음수일 때 실패 - 400")
        void usePoint_negativeAmount_fail() {
            assertThatThrownBy(() -> pointService.usePoint("user-01", -1))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("금액은 0보다 커야 합니다.")
                    .satisfies(e -> assertThat(((ServiceException) e).getStatus())
                            .isEqualTo(HttpStatus.BAD_REQUEST));
        }

        @Test
        @DisplayName("사용 후 거래 내역 저장")
        void usePoint_savesTransaction() {
            Point point = new Point("user-01", 200L);
            when(pointRepository.findById("user-01")).thenReturn(Optional.of(point));
            when(pointRepository.save(any(Point.class))).thenReturn(point);

            pointService.usePoint("user-01", 50);

            verify(pointTransactionRepository).save(any(PointTransaction.class));
        }
    }

    // ──────────────────────────────────────────
    // 거래 내역 조회
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("거래 내역 조회")
    class GetTransactions {

        @Test
        @DisplayName("거래 내역 없을 때 빈 리스트 반환")
        void getTransactions_empty() {
            when(pointTransactionRepository.findByUserIdOrderByTimestampDesc(
                    eq("user-01"), any(Pageable.class)))
                    .thenReturn(List.of());

            PointTransactionsResponse response = pointService.getTransactions("user-01", 20);

            assertThat(response.userId()).isEqualTo("user-01");
            assertThat(response.transactions()).isEmpty();
        }

        @Test
        @DisplayName("거래 내역 반환")
        void getTransactions_returnsItems() {
            PointTransaction tx = new PointTransaction(
                    "user-01", PointTransactionType.CHARGE, 100, 100L, Instant.now());
            ReflectionTestUtils.setField(tx, "id", 1L);
            when(pointTransactionRepository.findByUserIdOrderByTimestampDesc(
                    eq("user-01"), any(Pageable.class)))
                    .thenReturn(List.of(tx));

            PointTransactionsResponse response = pointService.getTransactions("user-01", 20);

            assertThat(response.transactions()).hasSize(1);
            assertThat(response.transactions().get(0).getType()).isEqualTo("CHARGE");
            assertThat(response.transactions().get(0).getAmount()).isEqualTo(100L);
        }

        @Test
        @DisplayName("limit이 200 초과 시 200으로 제한")
        void getTransactions_limitCapped() {
            when(pointTransactionRepository.findByUserIdOrderByTimestampDesc(
                    eq("user-01"), any(Pageable.class)))
                    .thenReturn(List.of());

            pointService.getTransactions("user-01", 999);

            verify(pointTransactionRepository).findByUserIdOrderByTimestampDesc(
                    eq("user-01"),
                    argThat(p -> p.getPageSize() == 200));
        }

        @Test
        @DisplayName("limit이 1 미만 시 1로 제한")
        void getTransactions_limitFloor() {
            when(pointTransactionRepository.findByUserIdOrderByTimestampDesc(
                    eq("user-01"), any(Pageable.class)))
                    .thenReturn(List.of());

            pointService.getTransactions("user-01", 0);

            verify(pointTransactionRepository).findByUserIdOrderByTimestampDesc(
                    eq("user-01"),
                    argThat(p -> p.getPageSize() == 1));
        }
    }
}