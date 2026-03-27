package com.example.spring_01_boot.remit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RemitRequestControllerTest {

    @Autowired
    MockMvc mockMvc;

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

        // 조회 테스트는 DB에 데이터가 있어야 하므로
        // 먼저 생성 요청을 보내 데이터를 세팅
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
            createRequest("testID-03", "testID-05", 20000); // 다른 수신자 — 조회 안 돼야 함

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
}