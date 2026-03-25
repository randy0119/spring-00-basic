package com.example.spring_01_boot.order.service;

import org.springframework.stereotype.Service;

import com.example.spring_01_boot.order.dto.PayResponse;
import com.example.spring_01_boot.order.repository.PayRepository;
import com.example.spring_01_boot.order.repository.entity.Pay;
import com.example.spring_01_boot.order.repository.entity.PaymentType;
import com.example.spring_01_boot.order.repository.entity.OrderStatus;
import com.example.spring_01_boot.order.repository.entity.PayStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.example.spring_01_boot.order.dto.OrderResponse;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {
    private final PayRepository payRepository;
    private final OrderService orderService;

    @Override
    public Long getPayIdFromOrderIdWherePayStatus(Long orderId, PayStatus payStatus) {
        return payRepository.findTopByOrderIdAndPayStatusOrderByCreatedAtDesc(orderId, payStatus)
            .map(Pay::getPayId)
            .orElseThrow(() -> new IllegalArgumentException("해당 상태의 결제 정보가 존재하지 않습니다."));
    }

    @Override
    @Transactional
    public PayResponse pay(Long orderId, PaymentType paymentType, String creditMethod, int amount) {
        // 주문 존재 및 상태 확인
        orderService.getOrder(orderId);

        // 이미 PAID 결제가 존재하면 중복 결제 방지
        if (payRepository.findTopByOrderIdAndPayStatusOrderByCreatedAtDesc(orderId, PayStatus.PAID).isPresent()) {
            throw new IllegalArgumentException("이미 결제된 주문입니다.");
        }

        // PayStatus payStatus = bank API(creditMethod, amount) 결과에 따라 결정;
        PayStatus payStatus = PayStatus.PAID;
        Pay pay = payRepository.save(new Pay(orderId, paymentType, payStatus, creditMethod, amount));

        if (payStatus != PayStatus.PAID) {
            throw new IllegalArgumentException("결제 실패");
        }

        // 주문 결제 처리
        orderService.pay(orderId);

        return new PayResponse(pay.getPayId(), pay.getOrderId(), pay.getPaymentType(), pay.getPayStatus(), pay.getCreatedAt());
    }

    @Override
    @Transactional
    public PayResponse cancel(Long orderId) {
        // 주문 존재 및 상태 확인
        orderService.getOrder(orderId);

        // 취소는 'PAID인 pay'에 대해서만 수행
        Long payId = Objects.requireNonNull(
            getPayIdFromOrderIdWherePayStatus(orderId, PayStatus.PAID),
            "payId"
        );
        Pay pay = payRepository.findById(payId)
            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
        pay.cancel();
        payRepository.save(pay);
        orderService.cancel(orderId);
        return new PayResponse(pay.getPayId(), pay.getOrderId(), pay.getPaymentType(), pay.getPayStatus(), pay.getCreatedAt());
    }
}
