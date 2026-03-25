package com.example.spring_01_boot.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.spring_01_boot.order.repository.entity.PaymentType;
import lombok.Getter;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;

@Getter
@AllArgsConstructor
public class PayRequest {
    @NotNull
    private Long orderId;
    @NotBlank
    private String paymentType;// 결제 종류(카드, 현금, 포인트, 상품권)
    @NotBlank
    private String creditMethod;    // 결제 수단(카드번호, 현금지불, 상품권번호, 포인트계좌)
    @Positive
    @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
    private int amount;             // 결제 금액
}
