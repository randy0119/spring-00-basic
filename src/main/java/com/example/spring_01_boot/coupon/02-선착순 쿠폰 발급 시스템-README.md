# Coupon (선착순 쿠폰 발급)

쿠폰을 생성하고, 이름 기준으로 조회·발급하며 발급 이력을 남기는 모듈입니다.  
상위 프로젝트: **Spring Boot 3.3.x**, **Java 17**, **Spring Data JPA**, 인메모리 **H2**.

과제 문서: `assignment/02-선착순 쿠폰 발급 시스템.md` — 발급 API는 과제 예시와 동일하게 **`POST /coupons/{couponId}/issue`** + JSON 바디입니다.

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

### 3. 아직 과제 전체와 차이가 나거나 보완 여지가 있는 부분

| 항목 | 현재 구현 | 과제/권장 방향 |
|------|-----------|----------------|
| **사용자 쿠폰 목록 / 쿠폰 사용 API** | 미구현 | `GET /users/{userId}/coupons`, `POST .../use` 등 |
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
├── controller/dto/          # newCouponRequest
├── service/                 # CouponService / CouponServiceImpl
├── repository/              # CouponRepository, CouponTransactionRepository
├── repository/entity/       # Coupon, CouponTransaction, CouponStatus
├── dto/                     # CouponCreateResponse, CouponIssueRequest/Response, CouponErrorResponse
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
