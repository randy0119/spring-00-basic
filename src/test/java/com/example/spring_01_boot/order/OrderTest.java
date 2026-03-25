package com.example.spring_01_boot.order;

import com.example.spring_01_boot.order.dto.OrderCreateRequest;
import com.example.spring_01_boot.order.dto.OrderResponse;
import com.example.spring_01_boot.order.dto.PayRequest;
import com.example.spring_01_boot.order.repository.PayRepository;
import com.example.spring_01_boot.order.repository.OrderRepository;
import com.example.spring_01_boot.order.repository.entity.Pay;
import com.example.spring_01_boot.order.repository.entity.PayStatus;
import com.example.spring_01_boot.order.repository.entity.OrderStatus;
import com.example.spring_01_boot.order.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderTest {
    private static final String USER_A = "order-user-a";
    private static final String USER_B = "order-user-b";
    private static final String USER_EMPTY = "order-user-empty";
    private static final String BASKET_A = "basket-a";
    private static final String BASKET_B = "basket-b";

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PayRepository payRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> orderRepository.deleteAll());
        transactionTemplate.executeWithoutResult(status -> payRepository.deleteAll());
    }

    @Test
    void createOrder_success_returnsFilledResponse() {
        OrderCreateRequest request = new OrderCreateRequest(USER_A, BASKET_A);
        OrderResponse response = orderService.createOrder(request.getUserId(), request.getBascketId());

        assertThat(response.getOrderId()).isNotNull();
        assertThat(response.getUserId()).isEqualTo(USER_A);
        assertThat(response.getBascketId()).isEqualTo(BASKET_A);
        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void createOrder_whenBasketIdAlreadyExists_throwsException() {
        orderService.createOrder(USER_A, BASKET_A);

        assertThatThrownBy(() -> orderService.createOrder(USER_B, BASKET_A))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이미 주문된 장바구니입니다.");
    }

    @Test
    void createOrder_whenBasketIdIsDifferent_createsAnotherOrder() {
        OrderResponse first = orderService.createOrder(USER_A, BASKET_A);
        OrderResponse second = orderService.createOrder(USER_A, BASKET_B);

        assertThat(first.getOrderId()).isNotEqualTo(second.getOrderId());
        assertThat(second.getBascketId()).isEqualTo(BASKET_B);
        assertThat(orderRepository.count()).isEqualTo(2);
    }

    @Test
    void getOrders_whenUserHasOrders_returnsOnlyThatUserOrders() {
        orderService.createOrder(USER_A, "basket-a-1");
        orderService.createOrder(USER_A, "basket-a-2");
        orderService.createOrder(USER_B, "basket-b-1");

        var result = orderService.getOrders(USER_A);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(order -> USER_A.equals(order.getUserId()));
        assertThat(result).extracting(OrderResponse::getBascketId)
            .containsExactlyInAnyOrder("basket-a-1", "basket-a-2");
    }

    @Test
    void getOrders_whenUserHasNoOrders_returnsEmptyList() {
        orderService.createOrder(USER_A, BASKET_A);

        var result = orderService.getOrders(USER_EMPTY);

        assertThat(result).isEmpty();
    }

    @Test
    void getOrders_whenNoOrdersExist_returnsEmptyList() {
        var result = orderService.getOrders(USER_A);

        assertThat(result).isEmpty();
    }

    @Test
    void pay_cancel_success_updatesOrderAndPayStatus() throws Exception {
        var order = orderService.createOrder(USER_A, BASKET_A);

        // pay
        PayRequest payRequest = new PayRequest(order.getOrderId(), "CREDIT_CARD", "4111-1111-1111-1111", 1000);
        mockMvc.perform(post("/pay")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(payRequest))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(order.getOrderId()))
            .andExpect(jsonPath("$.payStatus").value("PAID"));

        var refreshedOrder = orderRepository.findByOrderId(order.getOrderId());
        assertThat(refreshedOrder.getOrderStatus()).isEqualTo(OrderStatus.PAID);

        Pay savedPay = payRepository.findByOrderId(order.getOrderId()).orElseThrow();
        assertThat(savedPay.getPayStatus()).isEqualTo(PayStatus.PAID);

        // cancel
        mockMvc.perform(get("/pay/cancel").param("orderId", order.getOrderId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(order.getOrderId()))
            .andExpect(jsonPath("$.payStatus").value("CANCELED"));

        refreshedOrder = orderRepository.findByOrderId(order.getOrderId());
        assertThat(refreshedOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);

        savedPay = payRepository.findByOrderId(order.getOrderId()).orElseThrow();
        assertThat(savedPay.getPayStatus()).isEqualTo(PayStatus.CANCELED);
    }

    @Test
    void pay_whenPayRequestPaymentTypeInvalid_returnsBadRequest() throws Exception {
        var order = orderService.createOrder(USER_A, BASKET_A);

        PayRequest request = new PayRequest(order.getOrderId(), "NOT_A_REAL_TYPE", "any-credit", 1000);
        mockMvc.perform(post("/pay")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("결제 종류가 올바르지 않습니다."));

        var refreshedOrder = orderRepository.findByOrderId(order.getOrderId());
        assertThat(refreshedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(payRepository.findByOrderId(order.getOrderId())).isEmpty();
    }

    @Test
    void pay_cancel_whenOrderDoesNotExist_returnsBadRequest() throws Exception {
        Long nonExistingOrderId = 999999L;

        PayRequest payRequest = new PayRequest(nonExistingOrderId, "CREDIT_CARD", "any-credit", 1000);
        mockMvc.perform(post("/pay")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(payRequest))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("주문 정보를 찾을 수 없습니다."));

        mockMvc.perform(get("/pay/cancel").param("orderId", nonExistingOrderId.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("주문 정보를 찾을 수 없습니다."));

        assertThat(payRepository.count()).isEqualTo(0);
    }

    @Test
    void cancel_whenOrderIsCreatedButNotPaid_returnsBadRequest() throws Exception {
        var order = orderService.createOrder(USER_A, BASKET_A);

        mockMvc.perform(get("/pay/cancel").param("orderId", order.getOrderId().toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("해당 상태의 결제 정보가 존재하지 않습니다."));

        var refreshedOrder = orderRepository.findByOrderId(order.getOrderId());
        assertThat(refreshedOrder.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(payRepository.findByOrderId(order.getOrderId())).isEmpty();
    }

    @Test
    void pay_cancel_whenOrderAlreadyCanceled_returnsBadRequest() throws Exception {
        var order = orderService.createOrder(USER_A, BASKET_A);

        PayRequest payRequest = new PayRequest(order.getOrderId(), "CREDIT_CARD", "4111-1111-1111-1111", 1000);
        mockMvc.perform(post("/pay")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(payRequest))))
            .andExpect(status().isOk());

        mockMvc.perform(get("/pay/cancel").param("orderId", order.getOrderId().toString()))
            .andExpect(status().isOk());

        // pay after cancel
        mockMvc.perform(post("/pay")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(payRequest))))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"))
            .andExpect(jsonPath("$.message").value("서버 처리 중 오류가 발생했습니다."));

        // cancel again
        mockMvc.perform(get("/pay/cancel").param("orderId", order.getOrderId().toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("해당 상태의 결제 정보가 존재하지 않습니다."));

        var refreshedOrder = orderRepository.findByOrderId(order.getOrderId());
        assertThat(refreshedOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);

        Pay savedPay = payRepository.findByOrderId(order.getOrderId()).orElseThrow();
        assertThat(savedPay.getPayStatus()).isEqualTo(PayStatus.CANCELED);
    }

    @Test
    void pay_whenOrderAlreadyPaid_returnsBadRequest() throws Exception {
        var order = orderService.createOrder(USER_A, BASKET_A);

        PayRequest payRequest = new PayRequest(order.getOrderId(), "CREDIT_CARD", "4111-1111-1111-1111", 1000);
        mockMvc.perform(post("/pay")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(payRequest))))
            .andExpect(status().isOk());

        mockMvc.perform(post("/pay")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(payRequest))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value("이미 결제된 주문입니다."));
    }
}
