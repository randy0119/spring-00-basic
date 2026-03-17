### Spring Boot 학습 프로젝트 개요

이 프로젝트는 **스프링 부트를 활용해 REST API와 핵심 비즈니스 로직을 단계적으로 설계·구현하는 흐름**을 연습하기 위한 미니 백엔드입니다.  
각 단계는 Git 커밋으로 구분되어 있어, 처음 보는 사람도 **“API → 서비스 → 레포지토리 → 엔티티 → 설정”** 으로 내려가는 전형적인 개발 프로세스를 한눈에 따라갈 수 있습니다.

- Java 17 (권장), Spring Boot 3.3.x
- H2 인메모리 데이터베이스 + Spring Data JPA
- Lombok 사용
- 간단한 회원가입 API + 비밀번호 해싱(SHA-256) 저장

---

### 커밋 단위로 보는 학습 흐름

아래 커밋들은 **백엔드 기능 개발 시 자주 반복되는 템플릿**을 의도적으로 나눠 둔 것입니다.

1. **[1] 가장 기본적인 REST 컨트롤러 (`99b151d`)**
   - `UserController` 의 `/hello` 엔드포인트로  
     “요청을 받고 문자열을 응답하는” 가장 단순한 컨트롤러를 구현.
   - 목표: 스프링 부트에서 컨트롤러를 등록하고, URL/HTTP 메서드로 요청을 받는 흐름 이해.

2. **[2] application 설정 파일 (`4508557`)**
   - `application.yml` 을 도입하고, H2 인메모리 DB 및 H2 콘솔(`/h2-console`) 설정.
   - 목표: **환경설정(application.yml)** 을 통해  
     애플리케이션 이름, 데이터소스, 콘솔 등 런타임 동작을 제어하는 방법 학습.

3. **[3] entity 패키지 (JPA와 DB 연결) (`813aba8`)**
   - `Member` 엔티티 추가: JPA 어노테이션과 Lombok (`@Entity`, `@Id`, `@Builder`, `@Getter` …) 융합.
   - DB 테이블과 1:1 매핑되는 클래스를 설계하고, O-R 매핑 개념을 정리.
   - 목표: **도메인 모델(엔티티)** 을 통해 DB 구조를 자바 코드로 표현하는 방법 이해.

4. **[4] repository 인터페이스와 테스트 코드 작성 (`895a3f1`)**
   - `MemberRepository extends JpaRepository<Member, Long>` 로 CRUD 인터페이스 정의.
   - `MemberRepositoryTest` 로 저장/조회 동작을 검증.
   - 목표: **Repository 계층**을 통해 SQL을 직접 쓰지 않고도 JPA로 CRUD 를 구현·테스트하는 흐름 체험.

5. **[5] service 인터페이스와 구현체 (`b972360`)**
   - `UserService` 인터페이스와 `UserServiceImpl` 구현체 추가.
   - 컨트롤러와 레포지토리 사이 **비즈니스 로직 계층(Service)** 을 분리하고,
     “회원가입” 기능을 중심으로 서비스 시그니처를 설계.
   - 목표: Controller–Service–Repository 레이어드 아키텍처에서  
     서비스 계층이 어떤 책임을 갖는지 이해.

6. **[6] 컨트롤러와 서비스 연동 + DTO (`66f8084`)**
   - `joinRequest` DTO 추가: HTTP 요청 바디(JSON)를 전용 DTO 로 받도록 구조화.
   - `UserController` 의 `/join` 엔드포인트에서:
     - `@RequestBody joinRequest` 로 입력값 수신
     - `UserService.join(...)` 호출
   - `UserServiceImpl` 에서:
     - 비밀번호를 SHA-256 해시로 암호화
     - `Member` 엔티티를 빌더로 생성 후 `MemberRepository.save(...)` 로 저장
   - 목표:
     - **DTO → Service → Repository → Entity** 로 이어지는 전형적인 “회원가입” 플로우를 구현
     - 컨트롤러는 입·출력과 라우팅에 집중하고,  
       비즈니스 로직(검증/암호화/저장)은 서비스에 위임하는 구조를 체득.

---

### 이 프로젝트에서 배울 수 있는 것

- **REST API 기본 패턴**
  - `@RestController`, `@GetMapping`, `@PostMapping` 등으로 엔드포인트 설계
  - 요청 바디를 DTO로 받고, 서비스에 위임한 뒤 응답 반환

- **계층 분리(Controller–Service–Repository–Entity)**
  - 컨트롤러: HTTP 요청/응답, DTO 매핑
  - 서비스: 비즈니스 규칙(예: 비밀번호 해싱, 가입 결과 판단)
  - 레포지토리: DB CRUD (Spring Data JPA)
  - 엔티티: DB 테이블과 매핑되는 도메인 모델

- **환경 설정과 인메모리 DB**
  - `application.yml` 로 H2 데이터베이스 및 H2 콘솔 설정
  - 개발/학습용 DB 환경 구성 방법

- **Git 커밋을 활용한 단계적 학습**
  - 각 커밋이 하나의 **학습 단계**를 의미하도록 정리되어 있어,
    `git checkout <커밋ID>` 로 과정을 되짚어 보며 학습 가능.

---

### 추천 학습 순서

1. `README` 의 커밋 목록을 훑어보며 전체 구조를 파악한다.
2. 최신 코드 상태에서 애플리케이션을 실행해 본다:
   - `./mvnw spring-boot:run`
   - `POST /join` 으로 회원가입 요청 보내보기 (예: Postman, curl)
3. 컨트롤러 → DTO → 서비스 → 레포지토리 → 엔티티 순으로 코드를 읽으며  
   “데이터가 어떻게 흘러가는지” 추적한다.
4. 필요하면 특정 단계의 커밋으로 돌아가(`git checkout 895a3f1` 등)  
   그 시점의 코드와 현재 코드를 비교해 본다.

이 흐름을 몇 번 반복해 보면,  
**실제 백엔드 기능 개발에서도 그대로 활용할 수 있는 “스프링 부트 기능 개발 템플릿”** 이 몸에 익게 됩니다.

---

### [더 생각해보기!] 🚦 실제 서비스 백엔드 개발 시 반드시 신경 써야 할 핵심 기술/원칙

- **입력 검증(Validation) 필수**
  - DTO 레벨에서 `@Valid`, 커스텀 Validator 등으로 모든 입력값을 검증해야 안전하다.
  - 잘못된 데이터가 서비스/DB로 내려가는 것을 반드시 막을 것.

- **예외 처리(Exception Handling) 일관성**
  - `@ControllerAdvice` + `@ExceptionHandler`를 활용해, 모든 예외 상황에 대한 일관적(표준화)된 JSON 응답 구현 필요.
  - 에러 코드/메시지, HTTP 상태코드를 명확히 구분할 것.

- **비밀번호 평문 저장은 절대 금지**
  - 반드시 안전한 해시(SHA-256, bcrypt, argon2 등) + Salt 적용해서 암호화 저장.

- **트랜잭션 처리**
  - DB를 갱신하는 서비스 메서드에는 `@Transactional`을 꼭 사용해서 데이터 일관성 보장.

- **로그(LOG)와 모니터링**
  - 필요한 모든 요청/응답/에러에 대해 적절한 로그를 남기고, 민감한 정보 로그는 피할 것.
  - (운영 시 APM, Metrics, Alert 연동도 필수!)

- **권한/인증 처리**
  - 로그인/회원가입 이후에는 JWT, OAuth, 세션 등을 사용해 인증/인가 로직을 구현한다.
  - 관리자/일반 사용자, 소유자 등 역할별 접근 제어를 설계!

- **SQL Injection 등 보안 위협 대응**
  - JPA/QueryDSL/PreparedStatement처럼 파라미터 바인딩을 통해 쿼리 인젝션 차단.
  - 입력값 Sanitizing, XSS 방어, 민감 정보 마스킹 등도 신경 쓸 것.

- **엔드포인트 및 데이터 설계**
  - RESTful한 URI 설계, 에러 응답 표준화, API 문서화(Swagger/OpenAPI) 권장.

- **테스트 코드 작성**
  - 최소한의 통합(MockMvc), 단위 테스트를 작성하여 회귀 버그 방지 및 리팩토링 안전 대책 마련.

- **배포/운영 자동화**
  - 환경 분리(개발/운영) 및 CI/CD, 프로퍼티 별도관리, 이슈 대응을 위한 실시간 배포 전략 권장.

---

> 실무에서는 "빠르게 기능만 만들고 끝"보다,  
> 위와 같은 **보안·안정성·확장성** 원칙을 체계적으로 적용하는 것이  
> “프로다운 백엔드 개발”의 핵심입니다!