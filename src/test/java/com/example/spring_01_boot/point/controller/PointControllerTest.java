package com.example.spring_01_boot.point.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import com.example.spring_01_boot.point.repository.PointRepository;
import com.example.spring_01_boot.point.repository.PointTransactionRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest {

    private static final String TEST_USER = "integ-test-user";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    PointTransactionRepository pointTransactionRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    // 각 테스트 후 데이터 정리
    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            pointTransactionRepository.deleteByUserId(TEST_USER);
            pointRepository.deleteById(TEST_USER);
        });
    }

    // 충전 헬퍼
    private void charge(int amount) throws Exception {
        mockMvc.perform(post("/point/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "userId": "%s", "amount": %d }
                        """, TEST_USER, amount)));
    }

    // ──────────────────────────────────────────
    // 잔액 조회
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("잔액 조회")
    class GetPoint {

        @Test
        @DisplayName("신규 유저 조회 시 0 반환 및 행 생성 - 200 OK")
        void getPoint_newUser_returnsZero() throws Exception {
            mockMvc.perform(get("/point").param("userId", TEST_USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(0));
        }

        @Test
        @DisplayName("충전 후 잔액 조회 - 200 OK")
        void getPoint_afterCharge_returnsBalance() throws Exception {
            charge(500);

            mockMvc.perform(get("/point").param("userId", TEST_USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(500));
        }
    }

    // ──────────────────────────────────────────
    // 포인트 충전
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("포인트 충전")
    class ChargePoint {

        @Test
        @DisplayName("정상 충전 성공 - 200 OK")
        void chargePoint_success() throws Exception {
            mockMvc.perform(post("/point/charge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    { "userId": "%s", "amount": 100 }
                                    """, TEST_USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("충전이 완료되었습니다."))
                    .andExpect(jsonPath("$.balance").value(100));
        }

        @Test
        @DisplayName("누적 충전 후 잔액 합산 - 200 OK")
        void chargePoint_twice_accumulatesBalance() throws Exception {
            charge(100);

            mockMvc.perform(post("/point/charge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    { "userId": "%s", "amount": 200 }
                                    """, TEST_USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(300));
        }

        @Test
        @DisplayName("금액이 0일 때 실패 - 400")
        void chargePoint_zeroAmount_fail() throws Exception {
            mockMvc.perform(post("/point/charge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    { "userId": "%s", "amount": 0 }
                                    """, TEST_USER)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("금액은 0보다 커야 합니다."));
        }

        @Test
        @DisplayName("금액이 음수일 때 실패 - 400")
        void chargePoint_negativeAmount_fail() throws Exception {
            mockMvc.perform(post("/point/charge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    { "userId": "%s", "amount": -1 }
                                    """, TEST_USER)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("금액은 0보다 커야 합니다."));
        }
    }

    // ──────────────────────────────────────────
    // 포인트 사용
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("포인트 사용")
    class UsePoint {

        @Test
        @DisplayName("정상 사용 성공 - 200 OK")
        void usePoint_success() throws Exception {
            charge(200);

            mockMvc.perform(post("/point/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    { "userId": "%s", "amount": 50 }
                                    """, TEST_USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("포인트 사용이 완료되었습니다."))
                    .andExpect(jsonPath("$.balance").value(150));
        }

        @Test
        @DisplayName("잔액 부족 시 실패 - 409")
        void usePoint_insufficientBalance_fail() throws Exception {
            charge(10);

            mockMvc.perform(post("/point/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    { "userId": "%s", "amount": 100 }
                                    """, TEST_USER)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("잔액이 부족합니다."));
        }

        @Test
        @DisplayName("존재하지 않는 유저 사용 시 실패 - 400")
        void usePoint_noUser_fail() throws Exception {
            mockMvc.perform(post("/point/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    { "userId": "%s", "amount": 50 }
                                    """, TEST_USER)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("존재하지 않는 사용자 입니다."));
        }

        @Test
        @DisplayName("금액이 0일 때 실패 - 400")
        void usePoint_zeroAmount_fail() throws Exception {
            mockMvc.perform(post("/point/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    { "userId": "%s", "amount": 0 }
                                    """, TEST_USER)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("금액은 0보다 커야 합니다."));
        }
    }

    // ──────────────────────────────────────────
    // 거래 내역 조회
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("거래 내역 조회")
    class GetTransactions {

        @Test
        @DisplayName("거래 내역 없을 때 빈 리스트 - 200 OK")
        void getTransactions_empty() throws Exception {
            mockMvc.perform(get("/point/transactions").param("userId", TEST_USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(TEST_USER))
                    .andExpect(jsonPath("$.transactions").isEmpty());
        }

        @Test
        @DisplayName("충전 후 거래 내역 1건 - 200 OK")
        void getTransactions_afterCharge_hasOneEntry() throws Exception {
            charge(100);

            mockMvc.perform(get("/point/transactions").param("userId", TEST_USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(TEST_USER))
                    .andExpect(jsonPath("$.transactions[0].type").value("CHARGE"))
                    .andExpect(jsonPath("$.transactions[0].amount").value(100))
                    .andExpect(jsonPath("$.transactions[0].balanceAfter").value(100));
        }

        @Test
        @DisplayName("충전 후 사용 시 거래 내역 2건 최신순 - 200 OK")
        void getTransactions_chargeAndUse_twoEntriesLatestFirst() throws Exception {
            charge(200);
            mockMvc.perform(post("/point/use")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("""
                            { "userId": "%s", "amount": 50 }
                            """, TEST_USER)));

            mockMvc.perform(get("/point/transactions").param("userId", TEST_USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactions.length()").value(2))
                    .andExpect(jsonPath("$.transactions[0].type").value("USE"))    // 최신순
                    .andExpect(jsonPath("$.transactions[1].type").value("CHARGE"));
        }

        @Test
        @DisplayName("limit 파라미터 적용 - 200 OK")
        void getTransactions_limitApplied() throws Exception {
            charge(100);
            charge(200);
            charge(300);

            mockMvc.perform(get("/point/transactions")
                            .param("userId", TEST_USER)
                            .param("limit", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactions.length()").value(2));
        }
    }

    // ──────────────────────────────────────────
    // 시나리오 테스트
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("시나리오 테스트")
    class Scenario {

        @Test
        @DisplayName("충전 → 사용 → 잔액 조회 전체 흐름")
        void chargeUseThenGetBalance_fullFlow() throws Exception {
            // 1. 충전
            mockMvc.perform(post("/point/charge")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    { "userId": "%s", "amount": 1000 }
                                    """, TEST_USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(1000));

            // 2. 사용
            mockMvc.perform(post("/point/use")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    { "userId": "%s", "amount": 300 }
                                    """, TEST_USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(700));

            // 3. 잔액 조회
            mockMvc.perform(get("/point").param("userId", TEST_USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(700));

            // 4. 거래 내역 확인
            mockMvc.perform(get("/point/transactions").param("userId", TEST_USER))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactions.length()").value(2));
        }
    }
}