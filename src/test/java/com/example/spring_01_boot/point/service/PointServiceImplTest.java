package com.example.spring_01_boot.point.service;

import com.example.spring_01_boot.point.dto.PointOperationResponse;
import com.example.spring_01_boot.point.dto.PointTransactionsResponse;
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
    private static final String USER_A = "point-user-a";
    private static final String USER_B = "point-user-b";
    private static final String NO_TX_USER_ID = "no-tx-user";

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
            for (String userId : new String[] { TEST_USER_ID, USER_A, USER_B, NO_TX_USER_ID }) {
                pointTransactionRepository.deleteByUserId(userId);
                pointRepository.deleteById(userId);
            }
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

    @Test
    void getTransactions_whenNoTransactions_returnsEmptyList() {
        transactionTemplate.executeWithoutResult(status -> {
            pointTransactionRepository.deleteByUserId(NO_TX_USER_ID);
            pointRepository.deleteById(NO_TX_USER_ID);
        });

        PointTransactionsResponse response = pointService.getTransactions(NO_TX_USER_ID, 20);

        assertThat(response.userId()).isEqualTo(NO_TX_USER_ID);
        assertThat(response.transactions()).isEmpty();
        assertThat(pointTransactionRepository.countByUserId(NO_TX_USER_ID)).isZero();
    }

    @Test
    void getTransactions_afterAbnormalOperation_doesNotAppendRow() {
        pointRepository.deleteById(TEST_USER_ID);
        assertThat(pointService.chargePoint(TEST_USER_ID, 100).success()).isTrue();
        assertThat(pointTransactionRepository.countByUserId(TEST_USER_ID)).isEqualTo(1);

        assertThat(pointService.chargePoint(TEST_USER_ID, 0).success()).isFalse();

        assertThat(pointTransactionRepository.countByUserId(TEST_USER_ID)).isEqualTo(1);
        PointTransactionsResponse response = pointService.getTransactions(TEST_USER_ID, 20);
        assertThat(response.transactions()).hasSize(1);
        assertThat(response.transactions().get(0).amount()).isEqualTo(100L);
    }

    @Test
    void getTransactions_afterOneSuccessfulCharge_hasOneEntry() {
        pointRepository.deleteById(TEST_USER_ID);
        assertThat(pointService.chargePoint(TEST_USER_ID, 100).success()).isTrue();

        PointTransactionsResponse response = pointService.getTransactions(TEST_USER_ID, 20);

        assertThat(response.userId()).isEqualTo(TEST_USER_ID);
        assertThat(response.transactions()).hasSize(1);
        assertThat(response.transactions().get(0).type()).isEqualTo("CHARGE");
        assertThat(response.transactions().get(0).amount()).isEqualTo(100L);
        assertThat(response.transactions().get(0).balanceAfter()).isEqualTo(100L);
    }

    @Test
    void getTransactions_twoUsersInterleaved_threeEachAndSixTotalInDb() {
        transactionTemplate.executeWithoutResult(status -> {
            pointTransactionRepository.deleteByUserId(USER_A);
            pointTransactionRepository.deleteByUserId(USER_B);
            pointRepository.deleteById(USER_A);
            pointRepository.deleteById(USER_B);
        });

        assertThat(pointService.chargePoint(USER_A, 100).success()).isTrue();
        assertThat(pointService.chargePoint(USER_B, 100).success()).isTrue();
        assertThat(pointService.usePoint(USER_A, 10).success()).isTrue();
        assertThat(pointService.usePoint(USER_B, 10).success()).isTrue();
        assertThat(pointService.chargePoint(USER_A, 50).success()).isTrue();
        assertThat(pointService.chargePoint(USER_B, 50).success()).isTrue();

        PointTransactionsResponse forA = pointService.getTransactions(USER_A, 20);
        PointTransactionsResponse forB = pointService.getTransactions(USER_B, 20);

        assertThat(forA.transactions()).hasSize(3);
        assertThat(forB.transactions()).hasSize(3);
        assertThat(pointTransactionRepository.countByUserId(USER_A)).isEqualTo(3);
        assertThat(pointTransactionRepository.countByUserId(USER_B)).isEqualTo(3);
        assertThat(
            pointTransactionRepository.countByUserId(USER_A)
                + pointTransactionRepository.countByUserId(USER_B))
            .isEqualTo(6);
        assertThat(pointTransactionRepository.count()).isEqualTo(6);
    }
}
