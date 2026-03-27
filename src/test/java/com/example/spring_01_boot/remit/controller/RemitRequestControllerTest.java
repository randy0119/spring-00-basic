package com.example.spring_01_boot.remit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional  // 테스트 후 DB 롤백
class RemitRequestControllerTest {

    @Autowired
    MockMvc mockMvc;

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
    @DisplayName("본인에게 송금 요청 시 실패 - 400 Bad Request")
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
    @DisplayName("요청 금액이 0 이하일 때 실패 - 400 Bad Request")
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
    @DisplayName("요청 금액이 최대 한도 초과 시 실패 - 400 Bad Request")
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
