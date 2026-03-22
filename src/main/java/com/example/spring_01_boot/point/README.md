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
├── controller/          # REST API (pointContoller)
├── controller/dto/      # 요청 DTO (pointRequest)
├── service/             # PointService / PointServiceImpl
├── repository/          # PointRepository, PointTransactionRepository
├── repository/entity/   # Point, PointTransaction, PointTransactionType
└── dto/                   # API 응답 (PointOperationResponse, PointTransactionsResponse 등)
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

### 응답 예시 — 충전·사용 (`PointOperationResponse`)

성공:

```json
{
  "success": true,
  "message": "충전이 완료되었습니다.",
  "balance": 100
}
```

실패:

```json
{
  "success": false,
  "message": "잔액이 부족합니다.",
  "balance": null
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

## 실행 · 테스트

프로젝트 루트(`spring_00_basic`)에서:

```bash
./mvnw spring-boot:run
./mvnw test
```

포인트 관련 통합 테스트: `PointServiceImplTest`

H2 콘솔은 `application` 설정에 따라 `/h2-console` 에서 사용할 수 있습니다.

## 참고

- 과제 명세: `assignment/01-포인트 지갑 시스템.md`
- 컨트롤러 클래스명 `pointContoller`는 오타이며, 리팩터 시 `PointController` 등으로 바꾸는 것을 권장합니다.
