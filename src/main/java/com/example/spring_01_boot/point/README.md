# Point (포인트 지갑)

`userId` 단위로 포인트 잔액을 관리하고, 충전·사용·거래 내역을 제공하는 모듈입니다.  
상위 프로젝트: **Spring Boot 3.3.x**, **Java 17**, **Spring Data JPA**, 인메모리 **H2**.

## 도메인 요약

| 구분 | 설명 |
|------|------|
| **잔액** | `Point` 엔티티 — `userId`(PK)당 1행, `balance` |
| **거래** | `PointTransaction` — 성공한 충전/사용만 저장 (`CHARGE` / `USE`, 금액, 처리 후 잔액, 시각) |
| **규칙** | 충전·사용 금액은 양수만 허용. 잔액 부족 시 사용 실패. 실패 건은 거래 테이블에 남기지 않음. |

## 패키지 구조
```
point/
├── controller/          # REST API (PointController)
├── controller/dto/      # 요청 DTO (PointRequest)
├── service/             # PointService / PointServiceImpl
├── repository/          # PointRepository, PointTransactionRepository
├── repository/entity/   # Point, PointTransaction, PointTransactionType
└── dto/                 # API 응답 (PointOperationResponse, PointBalanceResponse, PointTransactionsResponse 등)
```

## API

기본 호스트 예: `http://localhost:8080`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `GET` | `/point?userId={id}` | 잔액 조회 (없으면 0으로 행 생성 후 반환) |
| `POST` | `/point/charge` | 충전 |
| `POST` | `/point/use` | 사용 |
| `GET` | `/point/transactions?userId={id}&limit=20` | 거래 내역 최신순 (`limit` 기본 20, 1~200) |

### 요청 예시 — 충전 / 사용

`Content-Type: application/json`
```json
{
  "userId": "user-01",
  "amount": 100
}
```

### 응답 예시 — 잔액 조회 (`PointBalanceResponse`)
```json
{
  "balance": 100
}
```

### 응답 예시 — 충전·사용 (`PointOperationResponse`)

성공:
```json
{
  "message": "충전이 완료되었습니다.",
  "balance": 100
}
```

실패 시 `GlobalExceptionHandler`가 아래 형식으로 반환:
```json
{
  "message": "잔액이 부족합니다."
}
```

### 응답 예시 — 거래 내역 (`PointTransactionsResponse`)
```json
{
  "userId": "user-01",
  "transactions": [
    {
      "transactionId": 1,
      "type": "CHARGE",
      "amount": 100,
      "balanceAfter": 100,
      "createdAt": "2026-03-22T06:00:00Z"
    }
  ]
}
```

## 리팩토링 사항

### 1. 클래스명 네이밍 컨벤션 수정
Java PascalCase 컨벤션에 맞게 수정했습니다.

| 변경 전 | 변경 후 |
|---------|---------|
| `pointContoller` | `PointController` |
| `pointRequest` | `PointRequest` |

### 2. 예외처리 구조 변경
기존 `success` 플래그 방식에서 `GlobalExceptionHandler` 기반 예외처리로 변경했습니다.

**변경 전** — 실패 시 응답 DTO에 `success: false`로 감싸서 반환 (항상 200 OK)
```json
{
  "success": false,
  "message": "잔액이 부족합니다.",
  "balance": null
}
```

**변경 후** — 실패 시 `ServiceException`을 throw, `GlobalExceptionHandler`가 적절한 HTTP 상태코드로 변환
```json
{
  "message": "잔액이 부족합니다."
}
```

| 상황 | 상태코드 |
|------|---------|
| 금액이 0 이하 | 400 Bad Request |
| 존재하지 않는 사용자 | 400 Bad Request |
| 잔액 부족 | 409 Conflict |

### 3. 잔액 조회 응답 DTO 추가
기존 `int` 직접 반환에서 `PointBalanceResponse`로 감싸 JSON 객체 형태로 통일했습니다.
```java
// 변경 전
public int getPoint(String userId)  →  Body: 700

// 변경 후
public PointBalanceResponse getPoint(String userId)  →  Body: {"balance": 700}
```

### 4. 유효성 검증 메시지 한국어 통일
`@Min`, `@Max` 어노테이션의 기본 영문 메시지를 한국어로 변경했습니다.
```java
@Min(value = 1, message = "조회 건수는 1 이상이어야 합니다.")
@Max(value = 200, message = "한번에 조회 가능한 최대 거래내역 갯수는 200개입니다.")
```

### 5. 테스트 추가
단위테스트(`PointServiceImplUnitTest`)와 통합테스트(`PointControllerTest`)를 분리하여 작성했습니다.

| 테스트 | 클래스 | 설명 |
|--------|--------|------|
| 단위 테스트 | `PointServiceImplUnitTest` | Mockito 기반, 비즈니스 로직 검증 |
| 통합 테스트 | `PointControllerTest` | SpringBootTest + MockMvc, HTTP 레이어부터 DB까지 검증 |

## 실행 · 테스트

프로젝트 루트(`spring_00_basic`)에서:
```bash
./mvnw spring-boot:run
./mvnw test
```

H2 콘솔은 `application` 설정에 따라 `/h2-console` 에서 사용할 수 있습니다.

## 참고

- 과제 명세: `assignment/01-포인트 지갑 시스템.md`