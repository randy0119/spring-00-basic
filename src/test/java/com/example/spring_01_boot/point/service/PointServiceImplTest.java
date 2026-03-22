package com.example.spring_01_boot.point.service;

import com.example.spring_01_boot.point.dto.PointOperationResponse;
import com.example.spring_01_boot.point.repository.PointRepository;
import com.example.spring_01_boot.point.repository.PointTransactionRepository;
import com.example.spring_01_boot.point.repository.entity.Point;
import com.example.spring_01_boot.point.repository.entity.PointTransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PointServiceImplTest {

    private static final String TEST_USER_ID = "point-test-user";

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            pointTransactionRepository.deleteByUserId(TEST_USER_ID);
            pointRepository.deleteById(TEST_USER_ID);
        });
    }

    @Test
    void getPoint_whenUserRowDoesNotExist_returnsZeroAndCreatesRow() {
        // given
        pointRepository.deleteById(TEST_USER_ID);

        // when
        int balance = pointService.getPoint(TEST_USER_ID);

        // then
        assertThat(balance).isEqualTo(0);

        Optional<Point> createdPoint = pointRepository.findById(TEST_USER_ID);
        assertThat(createdPoint).isPresent();
        assertThat(createdPoint.get().getBalance()).isEqualTo(0L);
    }

    @Test
    void getPoint_whenCalledTwice_doesNotCreateDuplicateRow() {
        // given
        pointRepository.deleteById(TEST_USER_ID);

        // when
        int firstBalance = pointService.getPoint(TEST_USER_ID);
        long countAfterFirstCall = pointRepository.count();
        int secondBalance = pointService.getPoint(TEST_USER_ID);
        long countAfterSecondCall = pointRepository.count();

        // then
        assertThat(firstBalance).isEqualTo(0);
        assertThat(secondBalance).isEqualTo(0);
        assertThat(countAfterSecondCall).isEqualTo(countAfterFirstCall);
        assertThat(pointRepository.findById(TEST_USER_ID)).isPresent();
    }

    @Test
    void chargePoint_success_increasesBalanceAndPersistsChargeTransaction() {
        pointRepository.deleteById(TEST_USER_ID);

        PointOperationResponse response = pointService.chargePoint(TEST_USER_ID, 100);

        assertThat(response.success()).isTrue();
        assertThat(response.balance()).isEqualTo(100L);
        assertThat(pointRepository.findById(TEST_USER_ID)).isPresent();
        assertThat(pointRepository.findById(TEST_USER_ID).get().getBalance()).isEqualTo(100L);
        assertThat(pointTransactionRepository.countByUserId(TEST_USER_ID)).isEqualTo(1);
        assertThat(pointTransactionRepository.countByUserIdAndTransactionType(TEST_USER_ID, PointTransactionType.CHARGE))
            .isEqualTo(1);
    }

    @Test
    void usePoint_withinBalance_succeedsAndPersistsUseTransaction() {
        pointRepository.deleteById(TEST_USER_ID);
        pointService.chargePoint(TEST_USER_ID, 200);

        PointOperationResponse response = pointService.usePoint(TEST_USER_ID, 50);

        assertThat(response.success()).isTrue();
        assertThat(response.balance()).isEqualTo(150L);
        assertThat(pointRepository.findById(TEST_USER_ID).get().getBalance()).isEqualTo(150L);
        assertThat(pointTransactionRepository.countByUserId(TEST_USER_ID)).isEqualTo(2);
        assertThat(pointTransactionRepository.countByUserIdAndTransactionType(TEST_USER_ID, PointTransactionType.CHARGE))
            .isEqualTo(1);
        assertThat(pointTransactionRepository.countByUserIdAndTransactionType(TEST_USER_ID, PointTransactionType.USE))
            .isEqualTo(1);
    }

    @Test
    void usePoint_exceedsBalance_failsWithoutUseTransaction() {
        pointRepository.deleteById(TEST_USER_ID);
        pointService.chargePoint(TEST_USER_ID, 10);

        PointOperationResponse response = pointService.usePoint(TEST_USER_ID, 100);

        assertThat(response.success()).isFalse();
        assertThat(response.message()).contains("부족");
        assertThat(response.balance()).isNull();
        assertThat(pointRepository.findById(TEST_USER_ID).get().getBalance()).isEqualTo(10L);
        assertThat(pointTransactionRepository.countByUserIdAndTransactionType(TEST_USER_ID, PointTransactionType.USE))
            .isZero();
        assertThat(pointTransactionRepository.countByUserIdAndTransactionType(TEST_USER_ID, PointTransactionType.CHARGE))
            .isEqualTo(1);
    }

    @Test
    void chargePoint_amountZero_failsAndDoesNotPersist() {
        pointRepository.deleteById(TEST_USER_ID);

        PointOperationResponse response = pointService.chargePoint(TEST_USER_ID, 0);

        assertThat(response.success()).isFalse();
        assertThat(response.message()).contains("0보다");
        assertThat(response.balance()).isNull();
        assertThat(pointRepository.findById(TEST_USER_ID)).isEmpty();
        assertThat(pointTransactionRepository.countByUserId(TEST_USER_ID)).isZero();
    }

    @Test
    void usePoint_negativeAmount_failsWithoutPersisting() {
        pointRepository.deleteById(TEST_USER_ID);

        PointOperationResponse response = pointService.usePoint(TEST_USER_ID, -1);

        assertThat(response.success()).isFalse();
        assertThat(response.message()).contains("0보다");
        assertThat(response.balance()).isNull();
        assertThat(pointTransactionRepository.countByUserId(TEST_USER_ID)).isZero();
    }
}
