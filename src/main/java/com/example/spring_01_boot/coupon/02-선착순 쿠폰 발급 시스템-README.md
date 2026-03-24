# Coupon (선착순 쿠폰 발급)

쿠폰을 생성하고, 발급하며 **사용자별 발급 이력(목록) 조회**까지 제공하는 모듈입니다.  
상위 프로젝트: **Spring Boot 3.3.x**, **Java 17**, **Spring Data JPA**, 인메모리 **H2**.

과제 문서: `assignment/02-선착순 쿠폰 발급 시스템.md` — 발급 API는 **`POST /coupons/{couponId}/issue`** + JSON 바디와 동일합니다. 목록 조회는 과제 예시 경로(`GET /users/{userId}/coupons`)와 다르게 **`GET /coupons/transactions`** 로 구현되어 있습니다(아래 API 표 참고).

---

## 주요 기술 선택사항

### 1. 쿠폰 생성 API

| 선택 | 이유 |
|------|------|
| **Spring MVC + `@Valid` + 요청 DTO** | JSON 바디를 `newCouponRequest`로 바인딩하고 Bean Validation으로 필수값·양수 수량을 검증합니다. |
| **JPA 엔티티 `Coupon`** | `couponName` **유니크**, `totalQuantity` / `issuedQuantity` / `expireDate` / `createdAt`을 한 행에서 관리합니다. |
| **상태 `CouponStatus`는 저장 컬럼 없이 계산** | `getStatus(now)`로 `AVAILABLE` / `SOLD_OUT` / `EXPIRED`를 조회 시점에 판정합니다. |
| **응답은 `CouponCreateResponse` record** | 과제 예시 필드(`couponId`, `name`, `totalQuantity`, `issuedQuantity`, `expiresAt`, `status`, `createdAt`)에 맞춥니다. |
| **`couponId`는 `"c-" + DB id`** | 외부 식별자 문자열을 별도 컬럼 없이 조합해 반환합니다. (필요 시 `publicId` 컬럼 분리 가능) |
| **동일 `name` 재요청 시 기존 행 반환** | `findByCouponName`으로 있으면 새로 저장하지 않고 그대로 응답합니다. (의도: 이름=자연키에 가까운 사용) |

### 2. 쿠폰 발급 API

| 선택 | 이유 |
|------|------|
| **REST: `POST /coupons/{couponId}/issue` + `CouponIssueRequest`** | 과제 예시와 맞추고, 변경에 안전한 JSON 바디(`userId`)를 사용합니다. |
| **외부 ID 파싱: `CouponIdParser`** | `c-{숫자}` ↔ DB PK 매핑을 한곳에서 처리하고, 잘못된 형식은 `400` + `INVALID_COUPON_ID`로 응답합니다. |
| **쿠폰 행 비관적 락** | `CouponRepository.findByIdForUpdate`로 `PESSIMISTIC_WRITE` — 동시 발급 시 `issuedQuantity` 경쟁을 직렬화합니다. |
| **중복 발급 방지** | `existsByUserIdAndCouponName` 선검사 + `coupon_transaction (user_id, coupon_name)` **유니크 제약**으로 이중 방어. |
| **도메인 예외 + `@RestControllerAdvice`** | 없음/충돌/형식 오류를 `CouponNotFoundException`(404), `CouponIssueConflictException`(409), `InvalidCouponIdException`(400)으로 구분하고 `CouponErrorResponse` JSON으로 반환합니다. |
| **수량·상태: `Coupon.getStatus` + `issue()`** | 만료·소진·중복 순서를 서비스에서 명시하고, `issue()`는 `AVAILABLE`일 때만 수량 증가합니다. |
| **발급 이력: `CouponTransaction`** | 성공 시 저장; 응답의 `userCouponId`는 `uc-{id}` 형식으로 반환합니다. |
| **`@Transactional` on `issueCoupon`** | 락·수량·이력 저장을 한 트랜잭션으로 묶습니다. |

### 3. 발급 쿠폰 목록 조회 API

| 선택 | 이유 |
|------|------|
| **`GET /coupons/transactions?userId=&limit=`** | 포인트 모듈의 `GET /point/transactions`와 유사한 쿼리 스타일로, 별도 경로 변수 없이 사용자·건수만 넘깁니다. |
| **데이터 소스: `CouponTransaction`** | 발급 성공 시 적재된 행만 조회합니다(발급 실패 건은 포함되지 않음). |
| **정렬: 발급 시각 내림차순** | `CouponTransactionRepository.findByUserIdOrderByTimestampDesc` + `Pageable`로 최신 발급이 앞에 오도록 합니다. |
| **`limit` 클램프** | 서비스에서 `1 ~ 200`으로 제한해 과도한 조회를 막습니다(포인트 거래 조회와 동일한 패턴). |
| **응답 DTO** | `CouponTransactionsResponse`(`userId`, `transactions[]`), 항목은 `CouponTransactionItemResponse`(`transactionId`, `couponName`, `issuedAt` 역할의 `timestamp`). |
| **`@Transactional(readOnly = true)`** | 조회 전용 트랜잭션으로 읽기 최적화 힌트를 줍니다. |

과제 예시 JSON은 `coupons` 배열에 `userCouponId`, `couponId`, `name`, `status`, `usedAt`, `expiresAt` 등이 있으나, 현재 구현은 **발급 이력 테이블 기준 최소 필드**만 반환합니다. 과제 스펙과 필드를 맞추려면 `Coupon` 조인·`userCouponId`(`uc-`)+`couponId`(`c-`) 포맷·만료일·`usedAt`(미사용 시 `null`) 등을 응답에 확장하면 됩니다.

### 4. 아직 과제 전체와 차이가 나거나 보완 여지가 있는 부분

| 항목 | 현재 구현 | 과제/권장 방향 |
|------|-----------|----------------|
| **목록 API 경로·응답 필드** | `GET /coupons/transactions`, 필드 최소화 | 예시: `GET /users/{userId}/coupons`, `userCouponId` / `couponId` / `name` / `expiresAt` / `usedAt` 등 |
| **쿠폰 사용 API** | 미구현 | `POST /users/{userId}/coupons/{userCouponId}/use` 등 |
| **`UserCoupon` 도메인** | `CouponTransaction`으로 발급 이력만 표현 | 사용 시각·상태(`USED`) 등은 별도 모델 확장 권장 |
| **낙관적 락·재시도** | 비관적 락만 사용 | 부하 특성에 따라 대안 검토 가능 |

---

## 도메인 요약

| 구분 | 설명 |
|------|------|
| **쿠폰** | `Coupon` — 이름 유니크, 총량·발급 수·만료·생성 시각 |
| **상태** | `CouponStatus`: `AVAILABLE`, `SOLD_OUT`, `EXPIRED` (계산) |
| **발급 이력** | `CouponTransaction` — 쿠폰명, 사용자, 발급 시각 |

---

## 패키지 구조

```
coupon/
├── controller/              # couponController
├── service/                 # CouponService / CouponServiceImpl
├── repository/              # CouponRepository, CouponTransactionRepository
├── repository/entity/       # Coupon, CouponTransaction, CouponStatus
├── dto/                     # newCouponRequest, CouponCreateResponse, CouponIssueRequest/Response, CouponErrorResponse, CouponTransactionsResponse, CouponTransactionItemResponse
├── exception/               # 도메인 예외
├── advice/                  # CouponExceptionHandler
└── support/                 # CouponIdParser
```

---

## API (현재 `couponController` 기준)

기본 호스트 예: `http://localhost:8080`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/coupons` | 쿠폰 생성 |
| `POST` | `/coupons/{couponId}/issue` | 쿠폰 발급 (`couponId` 예: `c-1`) |
| `GET` | `/coupons/transactions?userId={id}&limit={n}` | 해당 사용자 **발급 이력** 목록 (최신순, `limit` 필수·1~200으로 서버에서 클램프) |

### 쿠폰 생성 — 요청

`Content-Type: application/json`

```json
{
  "name": "WELCOME-5000",
  "totalQuantity": 100,
  "expiresAt": "2026-03-31T23:59:59Z"
}
```

### 쿠폰 생성 — 응답 (`CouponCreateResponse`)

```json
{
  "couponId": "c-1",
  "name": "WELCOME-5000",
  "totalQuantity": 100,
  "issuedQuantity": 0,
  "expiresAt": "2026-03-31T23:59:59Z",
  "status": "AVAILABLE",
  "createdAt": "2026-03-24T10:00:00Z"
}
```

### 쿠폰 발급 — 요청

`POST /coupons/c-1/issue`  
`Content-Type: application/json`

```json
{
  "userId": "u-1001"
}
```

### 쿠폰 발급 — 응답 (`CouponIssueResponse`)

```json
{
  "userId": "u-1001",
  "couponId": "c-1",
  "userCouponId": "uc-1",
  "status": "ISSUED",
  "issuedAt": "2026-03-24T10:00:00Z",
  "expiresAt": "2026-12-31T23:59:59Z"
}
```

### 오류 응답 (`CouponErrorResponse`)

| HTTP | `errorCode` (예) | 상황 |
|------|------------------|------|
| 400 | `INVALID_COUPON_ID` | `couponId` 형식 오류 |
| 404 | `COUPON_NOT_FOUND` | 존재하지 않는 쿠폰 |
| 409 | `COUPON_ALREADY_ISSUED` | 동일 사용자 재발급 |
| 409 | `COUPON_SOLD_OUT` | 수량 소진 |
| 409 | `COUPON_EXPIRED` | 만료 |

### 발급 쿠폰 목록 조회

`GET /coupons/transactions?userId=u-1001&limit=20`

- **파라미터**
  - `userId` (필수): 조회할 사용자 ID
  - `limit` (필수): 최대 건수 — 서비스에서 **1 이상 200 이하**로 보정됩니다.

### 발급 쿠폰 목록 — 응답 (`CouponTransactionsResponse`)

```json
{
  "userId": "u-1001",
  "transactions": [
    {
      "transactionId": 2,
      "couponName": "WELCOME-5000",
      "timestamp": "2026-03-24T11:00:00Z"
    },
    {
      "transactionId": 1,
      "couponName": "FLASH-1000",
      "timestamp": "2026-03-24T10:00:00Z"
    }
  ]
}
```

- `transactions`는 **`timestamp` 내림차순**(최신 발급이 배열 앞쪽)입니다.
- `transactionId`는 DB `CouponTransaction` PK이며, 발급 API 응답의 `userCouponId`(`uc-{id}`)와 같은 값입니다.
- 과제 예시의 `couponId`, `expiresAt`, `usedAt`, `status` 등은 현재 응답에 포함하지 않습니다(필요 시 `Coupon`과 조인해 확장).

---

## 향후 개발 후보

아래는 기능·도메인을 확장할 때의 체크리스트입니다. (현재 코드에는 미반영)

### 1. 할인 정책·사용 여부 필드

| 항목 | 제안 | 비고 |
|------|------|------|
| **`discountAmount`** | 정액 할인 금액 또는, `isPercent == true`일 때 할인 **비율(%)** 값 | 음수/0 처리, 상한(예: 최대 100%) 검증 필요 |
| **`isPercent`** | `true`: `discountAmount`를 퍼센트로 해석 / `false`: 고정 금액 할인 | 주문 금액 대비 계산은 **쿠폰 사용(결제) 시점** 서비스에서 수행 |
| **`isUsed`** | **발급 1건당** 사용 여부가 자연스러움 | 마스터 `Coupon`이 아니라 `CouponTransaction`(또는 별도 `UserCoupon`)에 `used` / `usedAt` 컬럼을 두는 편이 과제 스펙(`usedAt: null`)과도 맞음. 마스터에 두면 “이 쿠폰 종류 전체가 한 번만 쓰이는지” 같은 다른 의미가 되므로 설계 시 구분 |

**작업 예시**

- `Coupon` 엔티티에 `discountAmount`, `isPercent` 추가 → 생성 API 요청/응답 DTO 반영
- 발급 건에 `usedAt`(nullable), 조회 시 `status`: `ISSUED` / `USED` 계산 또는 저장
- 목록 API에서 할인 정보는 `Coupon`과 조인·또는 발급 시점 스냅샷으로 denormalize

### 2. 쿠폰 사용 기능

과제 확장: `POST /users/{userId}/coupons/{userCouponId}/use` (요청 예: `orderId`)

| 단계 | 내용 |
|------|------|
| **식별** | `userCouponId`(`uc-{id}`) 파싱 → 발급 행(`CouponTransaction` 등) 조회 |
| **권한** | 경로의 `userId`와 발급 행의 `userId` 일치 여부 |
| **가능 조건** | 미사용(`usedAt == null`), 쿠폰 마스터 만료 전(`Coupon.expireDate`) |
| **처리** | `usedAt`(및 선택적으로 `orderId`) 기록, 응답에 `status: USED` 등 |
| **예외** | 이미 사용 → `409`, 만료 → `409`/`400`, 없는 `userCouponId` → `404`, 잘못된 `uc-` 형식 → `400` |
| **동시성** | 동일 `userCouponId`에 대한 중복 사용 요청 → DB 유니크/비관적 락/멱등 키(`orderId`) 등으로 한 번만 성공 |
| **테스트** | 사용 성공, 재사용 실패, 타 사용자, 만료 쿠폰, 동시 사용 요청 |

`CouponExceptionHandler`에 사용 전용 `errorCode`(예: `COUPON_ALREADY_USED`)를 추가하면 API 일관성이 좋습니다.

---

## 실행 방법

프로젝트 루트(`spring_00_basic`)에서:

```bash
./mvnw spring-boot:run
```

---

## 참고

- 상세 요구사항·테스트 시나리오는 `assignment/02-선착순 쿠폰 발급 시스템.md`를 따릅니다.
- 포인트 모듈 문서 스타일은 `point/README.md`와 맞추었습니다.
- 리팩터링 변경 요약: `02-선착순 쿠폰 발급 시스템-리팩토링.md` (모듈 루트 `spring_01_boot` 기준).
