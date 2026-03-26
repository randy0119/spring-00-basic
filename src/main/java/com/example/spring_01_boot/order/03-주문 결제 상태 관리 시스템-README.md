## 주문 결제 상태 관리 시스템 (과제 03)

### 1) 구현 범위 요약
- **주문 생성**: 주문 생성 시 상태는 `CREATED`
- **주문 조회**: 사용자 기준 주문 목록 조회
- **결제**: `CREATED` 주문만 결제 가능, 결제 성공 시 주문 상태 `PAID`
- **취소**: “취소 가능한 결제(`PAID` pay)”가 존재할 때만 취소 진행, 취소 성공 시 주문 상태 `CANCELED`, pay 상태 `CANCELED`

### 2) 실행 방법
- **테스트 실행**

```bash
./mvnw test
```

- **애플리케이션 실행**

```bash
./mvnw spring-boot:run
```

### 3) 상태 전이 / 불변식
#### 3-1. `OrderStatus`
- `CREATED` → `PAID`
- `CREATED` → `CANCELED` (현재 구현에서는 결제 취소를 통해 `CANCELED`로 전이)
- `PAID` → `CANCELED`
- 그 외 전이는 예외로 처리

#### 3-2. `PayStatus`
- `PAID` → `CANCELED`

#### 3-3. 불변식(가정)
- 주문과 결제 상태 정합성은 불변식으로 보장된다고 가정합니다.
  - 예: `order:PAID` ↔ `pay:PAID`
  - 예: `order:CANCELED` ↔ `pay:CANCELED`
- 취소는 “주문에 대해 `PAID` 상태인 결제(pay) 1건”만 대상으로 수행합니다. 나머지는 결제 시도 이력으로 간주합니다.

### 4) API
#### 4-1. 주문 API (`OrderController`)
| 기능 | Method | Path | 요청 | 응답 |
|---|---:|---|---|---|
| 주문 생성 | POST | `/order/new` | `OrderCreateRequest` | `OrderResponse` |
| 주문 목록 조회 | GET | `/order/list?userId={userId}` | query | `List<OrderResponse>` |

##### 주문 생성 요청/응답 예시
- **Request**

```json
{
  "userId": "order-user-a",
  "bascketId": "basket-a"
}
```

- **Response**

```json
{
  "orderId": 1,
  "userId": "order-user-a",
  "bascketId": "basket-a",
  "orderStatus": "CREATED",
  "createdAt": "2026-03-25T07:32:57.493831Z"
}
```

#### 4-2. 결제 API (`PayController`)
> 과제 요구사항의 “주문 결제/취소 API”는 현재 구현에서 `PayController`로 노출되어 있습니다.

| 기능 | Method | Path | 요청 | 응답 |
|---|---:|---|---|---|
| 결제 | POST | `/pay` | `PayRequest` | `PayResponse` |
| 결제 취소 | GET | `/pay/cancel?orderId={orderId}` | query | `PayResponse` |

##### 결제 요청/응답 예시
- **Request**

```json
{
  "orderId": 1,
  "paymentType": "CREDIT_CARD",
  "creditMethod": "4111-1111-1111-1111",
  "amount": 1000
}
```

- **Response**

```json
{
  "payId": 1,
  "orderId": 1,
  "paymentType": "CREDIT_CARD",
  "payStatus": "PAID",
  "createdAt": "2026-03-25T07:32:57.493831Z"
}
```

##### 결제 취소 응답 예시

```json
{
  "payId": 1,
  "orderId": 1,
  "paymentType": "CREDIT_CARD",
  "payStatus": "CANCELED",
  "createdAt": "2026-03-25T07:32:57.493831Z"
}
```

### 5) 에러 응답 규격
`PayController`는 서비스/검증 실패를 아래 형태로 사용자에게 전달합니다.

```json
{
  "errorCode": "BAD_REQUEST",
  "message": "에러 메시지"
}
```

- **예: 결제 종류 오류**

```json
{
  "errorCode": "BAD_REQUEST",
  "message": "결제 종류가 올바르지 않습니다."
}
```

### 6) 주요 설계 선택
- **도메인 분리**: 주문(`Order`)과 결제(`Pay`)는 책임이 달라 `OrderService`/`PayService`로 분리했습니다.
- **취소 로직 분리**: `cancel(orderId)`에서 “취소 가능한 결제(`PAID`)”를 먼저 검증해 `payId`를 구하고, 이후에는 검증된 `payId`만 대상으로 취소 전이를 수행하도록 분리했습니다.
  - 관련 메서드: `PayService#getPayIdFromOrderIdWherePayStatus(...)`

### 7) 테스트 전략
- 테스트는 `@SpringBootTest` 기반 통합 테스트로 작성했습니다.
- 주문 도메인 검증은 서비스 레벨(`OrderService`)에서, 결제는 컨트롤러 입력/예외 응답까지 포함하기 위해 `MockMvc`로 검증합니다.
- 파일: `spring_00_basic/src/test/java/com/example/spring_01_boot/order/OrderTest.java`
  - **정상 흐름**: 주문 생성 → 결제 → 취소
  - **입력 오류**: `paymentType` 파싱 실패 시 400 + 메시지
  - **없는 주문**: 결제/취소 요청 시 400 + 메시지
  - **중복/재요청**: 이미 결제/취소된 상태에서의 결제/취소 재요청 검증

### 8) 확장 요구사항(미구현) / 개선 포인트
- **부분 취소**: 금액/부분 환불 모델링 필요(`Pay` 다건/부분 상태, 합계 검증 등)
- **결제 실패 이력 저장**: `PayStatus.FAILED` 생성/누적, 실패 사유/코드 저장, 재시도 전략(멱등성 키 등)
