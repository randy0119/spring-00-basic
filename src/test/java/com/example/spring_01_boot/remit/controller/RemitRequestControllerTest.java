package com.example.spring_01_boot.remit.controller;

import com.example.spring_01_boot.point.repository.PointRepository;
import com.example.spring_01_boot.point.repository.PointTransactionRepository;
import com.example.spring_01_boot.remit.client.PointClient;
import com.example.spring_01_boot.remit.repository.RemitRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class RemitRequestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @LocalServerPort
    int port;

    @Autowired
    PointClient pointClient;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    PointTransactionRepository pointTransactionRepository;

    @Autowired
    RemitRequestRepository remitRequestRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pointClient, "pointServiceUrl", "http://localhost:" + port);
    }

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            pointTransactionRepository.deleteAll();
            pointRepository.deleteAll();
            remitRequestRepository.deleteAll();
        });
    }

    // ──────────────────────────────────────────
    // 공통 헬퍼
    // ──────────────────────────────────────────
    private String createRequestAndGetHashCode(String requesterId, String receiverId, int amount) throws Exception {
        MvcResult result = mockMvc.perform(post("/remit/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                    "requesterId": "%s",
                                    "receiverId": "%s",
                                    "amount": %d
                                }
                                """, requesterId, receiverId, amount)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("remitHashCode").asText();
    }

    private void chargePoint(String userId, int amount) throws Exception {
        mockMvc.perform(post("/point/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "userId": "%s", "amount": %d }
                        """, userId, amount)));
    }

    private void createRequest(String requesterId, String receiverId, int amount) throws Exception {
        mockMvc.perform(post("/remit/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                            "requesterId": "%s",
                            "receiverId": "%s",
                            "amount": %d
                        }
                        """, requesterId, receiverId, amount)));
    }

    // ──────────────────────────────────────────
    // 송금 요청 생성
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("송금 요청 생성")
    class CreateRemitRequest {

        @Test
        @DisplayName("정상 송금 요청 생성 - 200 OK")
        void createRemitRequest_success() throws Exception {
            mockMvc.perform(post("/remit/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "requesterId": "testID-01",
                                        "receiverId": "testID-02",
                                        "amount": 10000
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.requesterId").value("testID-01"))
                    .andExpect(jsonPath("$.receiverId").value("testID-02"))
                    .andExpect(jsonPath("$.amount").value(10000))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.remitHashCode").isNotEmpty())
                    .andExpect(jsonPath("$.message").value("정상적으로 송금 요청했어요."));
        }

        @Test
        @DisplayName("본인에게 송금 요청 시 실패 - 400")
        void createRemitRequest_selfRemit_fail() throws Exception {
            mockMvc.perform(post("/remit/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "requesterId": "testID-01",
                                        "receiverId": "testID-01",
                                        "amount": 10000
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("송금요청 대상이 본인이에요."));
        }

        @Test
        @DisplayName("요청 금액이 0 이하일 때 실패 - 400")
        void createRemitRequest_invalidAmount_zero_fail() throws Exception {
            mockMvc.perform(post("/remit/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "requesterId": "testID-01",
                                        "receiverId": "testID-02",
                                        "amount": 0
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("요청 금액은 0보다 커야 해요."));
        }

        @Test
        @DisplayName("요청 금액이 최대 한도 초과 시 실패 - 400")
        void createRemitRequest_invalidAmount_overMax_fail() throws Exception {
            mockMvc.perform(post("/remit/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "requesterId": "testID-01",
                                        "receiverId": "testID-02",
                                        "amount": 10000001
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("1회 최대 송금 요청 금액은 10,000,000원을 넘길 수 없어요."));
        }

        @Test
        @DisplayName("requesterId가 null일 때 실패 - 400")
        void createRemitRequest_nullRequesterId_fail() throws Exception {
            mockMvc.perform(post("/remit/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "requesterId": null,
                                        "receiverId": "testID-02",
                                        "amount": 10000
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("amount가 정확히 최대값일 때 성공 - 경계값")
        void createRemitRequest_maxAmount_success() throws Exception {
            mockMvc.perform(post("/remit/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "requesterId": "testID-01",
                                        "receiverId": "testID-02",
                                        "amount": 10000000
                                    }
                                    """))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("amount가 음수일 때 실패 - 400")
        void createRemitRequest_negativeAmount_fail() throws Exception {
            mockMvc.perform(post("/remit/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "requesterId": "testID-01",
                                        "receiverId": "testID-02",
                                        "amount": -1
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("요청 금액은 0보다 커야 해요."));
        }

        @Test
        @DisplayName("requesterId가 빈 문자열일 때 실패 - 400")
        void createRemitRequest_blankRequesterId_fail() throws Exception {
            mockMvc.perform(post("/remit/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "requesterId": "",
                                        "receiverId": "testID-02",
                                        "amount": 10000
                                    }
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    // ──────────────────────────────────────────
    // 송금 요청 목록 조회
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("송금 요청 목록 조회")
    class GetRemitRequests {

        @Test
        @DisplayName("요청자ID로 조회 성공 - 200 OK")
        void getRemitRequests_byRequesterId_success() throws Exception {
            createRequest("testID-03", "testID-04", 10000);

            mockMvc.perform(get("/remit")
                            .param("requesterId", "testID-03"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].requesterId").value("testID-03"))
                    .andExpect(jsonPath("$[0].receiverId").value("testID-04"));
        }

        @Test
        @DisplayName("수신자ID로 조회 성공 - 200 OK")
        void getRemitRequests_byReceiverId_success() throws Exception {
            createRequest("testID-03", "testID-04", 10000);
            createRequest("testID-05", "testID-04", 20000);

            mockMvc.perform(get("/remit")
                            .param("receiverId", "testID-04"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].receiverId").value("testID-04"))
                    .andExpect(jsonPath("$[1].receiverId").value("testID-04"));
        }

        @Test
        @DisplayName("요청자ID + 수신자ID 둘 다 입력 시 조회 성공 - 200 OK")
        void getRemitRequests_byBoth_success() throws Exception {
            createRequest("testID-03", "testID-04", 10000);
            createRequest("testID-03", "testID-05", 20000);

            mockMvc.perform(get("/remit")
                            .param("requesterId", "testID-03")
                            .param("receiverId", "testID-04"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].requesterId").value("testID-03"))
                    .andExpect(jsonPath("$[0].receiverId").value("testID-04"));
        }

        @Test
        @DisplayName("요청자ID와 수신자ID 둘 다 없을 때 실패 - 400")
        void getRemitRequests_bothNull_fail() throws Exception {
            mockMvc.perform(get("/remit"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("요청자ID 또는 수신자ID를 입력해주세요."));
        }

        @Test
        @DisplayName("요청자ID와 수신자ID가 동일할 때 실패 - 400")
        void getRemitRequests_sameId_fail() throws Exception {
            mockMvc.perform(get("/remit")
                            .param("requesterId", "testID-03")
                            .param("receiverId", "testID-03"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("요청자ID와 수신자ID를 확인해주세요."));
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 리스트 반환 - 200 OK")
        void getRemitRequests_notFound_emptyList() throws Exception {
            mockMvc.perform(get("/remit")
                            .param("requesterId", "없는ID"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    // ──────────────────────────────────────────
    // 송금 요청 수락/거절
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("송금 요청 수락/거절")
    class DecisionRemitRequest {

        @Test
        @DisplayName("정상 수락 성공 - 200 OK")
        void decisionRemitRequest_accept_success() throws Exception {
            chargePoint("testID-02", 50000);
            String hashCode = createRequestAndGetHashCode("testID-01", "testID-02", 10000);

            mockMvc.perform(post("/remit/decision")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    {
                                        "remitHashcode": "%s",
                                        "receiverId": "testID-02",
                                        "decision": "ACCEPT"
                                    }
                                    """, hashCode)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"))
                    .andExpect(jsonPath("$.message").value("송금이 완료됐어요."));
        }

        @Test
        @DisplayName("정상 거절 성공 - 200 OK")
        void decisionRemitRequest_reject_success() throws Exception {
            String hashCode = createRequestAndGetHashCode("testID-01", "testID-02", 10000);

            mockMvc.perform(post("/remit/decision")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    {
                                        "remitHashcode": "%s",
                                        "receiverId": "testID-02",
                                        "decision": "REJECT"
                                    }
                                    """, hashCode)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJECTED"))
                    .andExpect(jsonPath("$.message").value("송금 요청이 거절됐어요."));
        }

        @Test
        @DisplayName("존재하지 않는 송금 요청 - 400")
        void decisionRemitRequest_notFound_fail() throws Exception {
            mockMvc.perform(post("/remit/decision")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "remitHashcode": "rmt_notexist",
                                        "receiverId": "testID-02",
                                        "decision": "ACCEPT"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("올바른 송금요청 해시번호와 수신자 ID인지 확인해주세요."));
        }

        @Test
        @DisplayName("수신자 잔고 부족 시 수락 실패 - 409")
        void decisionRemitRequest_insufficientBalance_fail() throws Exception {
            chargePoint("testID-02", 5000);  // 10000보다 적게 충전
            String hashCode = createRequestAndGetHashCode("testID-01", "testID-02", 10000);

            mockMvc.perform(post("/remit/decision")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    {
                                        "remitHashcode": "%s",
                                        "receiverId": "testID-02",
                                        "decision": "ACCEPT"
                                    }
                                    """, hashCode)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("잔고를 확인해주세요."));
        }

        @Test
        @DisplayName("이미 거절된 요청에 다시 수락 시도 시 실패 - 409")
        void decisionRemitRequest_alreadyProcessed_fail() throws Exception {
            // 1. 송금 요청 생성
            String hashCode = createRequestAndGetHashCode("testID-01", "testID-02", 10000);

            // 2. 거절
            mockMvc.perform(post("/remit/decision")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                            {
                                "remitHashcode": "%s",
                                "receiverId": "testID-02",
                                "decision": "REJECT"
                            }
                            """, hashCode)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJECTED"));

            // 3. 거절된 요청에 다시 수락 시도
            mockMvc.perform(post("/remit/decision")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                            {
                                "remitHashcode": "%s",
                                "receiverId": "testID-02",
                                "decision": "ACCEPT"
                            }
                            """, hashCode)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("이미 처리된 요청이에요."));
        }

        @Test
        @DisplayName("이미 수락된 요청에 다시 수락 시도 시 실패 - 409")
        void decisionRemitRequest_alreadyAccepted_fail() throws Exception {
            // 1. 수신자 포인트 충전
            chargePoint("testID-02", 50000);

            // 2. 송금 요청 생성
            String hashCode = createRequestAndGetHashCode("testID-01", "testID-02", 10000);

            // 3. 수락
            mockMvc.perform(post("/remit/decision")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                            {
                                "remitHashcode": "%s",
                                "receiverId": "testID-02",
                                "decision": "ACCEPT"
                            }
                            """, hashCode)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"));

            // 4. 이미 수락된 요청에 다시 수락 시도
            mockMvc.perform(post("/remit/decision")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                            {
                                "remitHashcode": "%s",
                                "receiverId": "testID-02",
                                "decision": "ACCEPT"
                            }
                            """, hashCode)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("이미 처리된 요청이에요."));
        }
    }

    // ──────────────────────────────────────────
    // 시나리오 테스트
    // ──────────────────────────────────────────
    @Nested
    @DisplayName("시나리오 테스트")
    class Scenario {

        @Test
        @DisplayName("송금 요청 생성 → 수락 → 잔액 확인 전체 흐름")
        void createAndAccept_fullFlow() throws Exception {
            chargePoint("testID-02", 50000);
            String hashCode = createRequestAndGetHashCode("testID-01", "testID-02", 10000);

            mockMvc.perform(post("/remit/decision")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("""
                                    {
                                        "remitHashcode": "%s",
                                        "receiverId": "testID-02",
                                        "decision": "ACCEPT"
                                    }
                                    """, hashCode)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"));

            // 수신자 잔액 차감 확인 (50000 - 10000 = 40000)
            mockMvc.perform(get("/point").param("userId", "testID-02"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(40000));

            // 요청자 잔액 충전 확인 (0 + 10000 = 10000)
            mockMvc.perform(get("/point").param("userId", "testID-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(10000));
        }
    }
}