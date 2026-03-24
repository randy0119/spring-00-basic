package com.example.spring_01_boot.order;

import com.example.spring_01_boot.order.dto.OrderCreateRequest;
import com.example.spring_01_boot.order.dto.OrderResponse;
import com.example.spring_01_boot.order.repository.OrderRepository;
import com.example.spring_01_boot.order.repository.entity.OrderStatus;
import com.example.spring_01_boot.order.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class OrderTest {
    private static final String USER_A = "order-user-a";
    private static final String USER_B = "order-user-b";
    private static final String BASKET_A = "basket-a";
    private static final String BASKET_B = "basket-b";

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> orderRepository.deleteAll());
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
}
