# 송금 요청 시스템

사용자 간 송금 요청을 생성하고, 수락·거절할 수 있는 모듈입니다.  
상위 프로젝트: **Spring Boot 3.3.x**, **Java 17**, **Spring Data JPA**, 인메모리 **H2**.

---

## 도메인 요약

| 구분 | 설명 |
|------|------|
| **송금 요청** | `RemitRequest` 엔티티 — 요청자/수신자/금액/상태/만료시각 관리 |
| **상태** | `PENDING` → `ACCEPTED` / `REJECTED` / `EXPIRED` |
| **포인트 연동** | 수락 시 `PointClient`를 통해 포인트 API 호출 |
| **규칙** | 자기 자신에게 요청 불가. 생성 후 10분 경과 시 만료. 수락 시 수신자 잔액 검증. 실패 건은 상태 변경 없음. |

---

## 패키지 구조
```
remit/
├── Controller/              # REST API (RemitRequestController)
├── client/                  # 포인트 서비스 HTTP 클라이언트 (PointClient)
├── dto/                     # 요청/응답 DTO (RemitRequest, RemitDecisionRequest, RemitRequestResponse)
├── service/                 # RemitRequestService / RemitRequestServiceImpl
└── repository/              # RemitRequestRepository
    └── entity/              # RemitRequest, RemitRequestStatus
```

---

## API

기본 호스트 예: `http://localhost:8080`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/remit/create` | 송금 요청 생성 |
| `GET` | `/remit?requesterId={id}&receiverId={id}` | 송금 요청 목록 조회 |
| `POST` | `/remit/decision` | 송금 요청 수락/거절 |

### 요청 예시 — 송금 요청 생성
```json
{
  "requesterId": "user-01",
  "receiverId": "user-02",
  "amount": 10000
}
```

### 요청 예시 — 송금 요청 수락/거절
```json
{
  "remitHashcode": "rmt_8f3d92ab1c",
  "receiverId": "user-02",
  "decision": "ACCEPT"
}
```

### 응답 예시 — 송금 요청 생성/수락/거절 (`RemitRequestResponse`)
```json
{
  "remitHashCode": "rmt_8f3d92ab1c",
  "requesterId": "user-01",
  "receiverId": "user-02",
  "amount": 10000,
  "status": "PENDING",
  "createdAt": "2026-03-27T14:00:00Z",
  "expiredAt": "2026-03-27T14:10:00Z",
  "message": "정상적으로 송금 요청했어요."
}
```

---

## 상태 전이
```
PENDING → ACCEPTED   수락 성공
PENDING → REJECTED   거절
PENDING → EXPIRED    만료 (생성 후 10분 경과)

ACCEPTED / REJECTED / EXPIRED → 변경 불가
```

---

## 예외처리

`GlobalExceptionHandler` 기반으로 상황별 HTTP 상태코드를 반환합니다.

| 상황 | 상태코드 |
|------|---------|
| 정상 생성 / 수락 / 거절 | 200 OK |
| 자기 자신에게 요청 | 400 Bad Request |
| 요청 금액 오류 (0 이하 / 한도 초과) | 400 Bad Request |
| 필드 누락 / 빈 값 | 400 Bad Request |
| 존재하지 않는 요청 | 400 Bad Request |
| 응답 권한 없음 | 403 Forbidden |
| 만료된 요청 | 410 Gone |
| 수신자 잔액 부족 | 409 Conflict |
| 이미 처리된 요청 | 409 Conflict |

---

## 포인트 연동 구조

수락 시 `PointClient`(RestTemplate)를 통해 포인트 API를 HTTP로 호출합니다.  
MSA 전환 시 `application.properties`의 URL만 변경하면 됩니다.
```
수락 흐름:
1. GET /point?userId={receiverId}   수신자 잔액 확인
2. POST /point/use                  수신자 잔액 차감
3. POST /point/charge               요청자 잔액 충전
```
```properties
# 현재 — 같은 서버
point.service.url=http://localhost:8080

# MSA 전환 후 — Point 서비스 별도 서버
point.service.url=http://point-service:8080
```

---

## 설계 결정사항

**remitHashCode 생성 전략**

외부 노출용 ID로 DB PK 대신 UUID 기반 해시코드를 사용합니다.
```
rmt_ + UUID 32자 중 앞 10자
예) rmt_8f3d92ab1c
```

DB `unique` 제약이 충돌 방어 1차선입니다. 트래픽이 수백만 건 이상으로 커지면 ULID 도입을 권장합니다.

**예외처리 구조**

서비스에서 `ServiceException`을 throw하고, `GlobalExceptionHandler`가 HTTP 상태코드로 변환합니다.
```
ServiceException (throw)
        ↓
GlobalExceptionHandler (catch)
        ↓
클라이언트 (적절한 상태코드 수신)
```

**트랜잭션 전략**

| 메서드 | 어노테이션 |
|--------|-----------|
| 생성, 수락/거절 | `@Transactional` |
| 목록 조회 | `@Transactional(readOnly = true)` |

수락 시 포인트 차감 + 충전 + 상태 변경이 하나의 트랜잭션으로 묶입니다.

---

## 테스트

### 단위 테스트 (`RemitRequestServiceImplTest`)

Mockito 기반으로 비즈니스 로직을 검증합니다. `PointClient`도 Mock으로 처리합니다.

| 분류 | 테스트 케이스 |
|------|-------------|
| 송금 요청 생성 | 정상 생성, 본인 요청, 금액 0 이하, 한도 초과 |
| 목록 조회 | 요청자/수신자/둘 다 조회, null, 동일 ID |
| 수락/거절 | 정상 수락, 거절, 존재하지 않는 요청, 만료, 잔고 부족 |

### 통합 테스트 (`RemitRequestControllerTest`)

`SpringBootTest(RANDOM_PORT)` + MockMvc로 HTTP 레이어부터 DB까지 검증합니다.  
`PointClient`가 실제 HTTP 호출을 하므로 `@Transactional` 대신 `@AfterEach`로 데이터를 정리합니다.

| 분류 | 테스트 케이스 |
|------|-------------|
| 송금 요청 생성 | 정상, 본인, 금액 오류, null, 경계값, 음수, 빈 문자열 |
| 목록 조회 | 요청자/수신자/둘 다, null, 동일 ID, 없는 ID |
| 수락/거절 | 정상 수락, 거절, 없는 요청, 잔고 부족 |
| 시나리오 | 생성 → 수락 → 양쪽 잔액 확인 전체 흐름 |

---

## 실행 · 테스트

프로젝트 루트(`spring_00_basic`)에서:
```bash
./mvnw spring-boot:run
./mvnw test
```

H2 콘솔은 `application` 설정에 따라 `/h2-console`에서 사용할 수 있습니다.

---

## 참고

- 과제 명세: `assignment/04-송금 요청 시스템.md`