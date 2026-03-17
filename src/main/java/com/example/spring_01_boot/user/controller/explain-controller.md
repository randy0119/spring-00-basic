### 📍 현재 위치

`spring_01_boot/src/main/java/com/example/spring_01_boot/user/controller` 는  
`user` 도메인에서 **웹 요청(HTTP 요청)을 담당하는 컨트롤러 클래스들**이 모여 있는 패키지입니다.

즉, 브라우저 · 앱 · 외부 서비스가 호출하는 **API 입구**에 해당합니다.

---

### 🌐 컨트롤러(Controller)란?

컨트롤러는 한 줄로 말하면 **“HTTP 요청을 받아, 적절한 서비스 로직을 호출하고, 응답을 돌려주는 계층”** 입니다.

스프링 부트에서는 주로 다음과 같은 어노테이션으로 표시합니다.

- `@RestController` : JSON 응답 API를 만드는 컨트롤러
- `@Controller`     : 템플릿(HTML View)을 반환하는 컨트롤러

컨트롤러의 역할은 다음과 같습니다.

- 클라이언트로부터 **URL + HTTP Method(GET/POST/PUT/DELETE 등)** 조합으로 들어오는 요청을 받는다.
- 요청 파라미터 / 바디를 **DTO나 파라미터로 매핑**한다.
- 도메인 로직을 가진 **Service 계층을 호출**한다.
- 처리 결과를 JSON, View, 상태코드 등으로 **HTTP 응답으로 변환**하여 반환한다.

---

### 🧩 이 패키지에서 다루는 것들

`user` 도메인 컨트롤러 패키지에는 보통 아래와 같은 클래스들이 위치합니다.

- `UserController`
  - 사용자 조회, 회원가입, 수정, 삭제 등 **사용자 관련 HTTP API 엔드포인트** 정의
  - 예: `GET /users`, `POST /users`, `GET /users/{id}` …

필요에 따라 다음과 같이 더 세분화할 수도 있습니다.

- `UserAuthController` : 로그인/로그아웃, 토큰 재발급 등 인증 관련 엔드포인트
- `UserProfileController` : 프로필, 설정 변경 등 사용자 정보 관련 엔드포인트

---

### 🧭 컨트롤러와 다른 계층의 관계

전형적인 스프링 부트 레이어드 아키텍처에서 컨트롤러는 다음과 같이 위치합니다.

1. **Controller**
   - HTTP 요청/응답을 다룬다.
   - 요청 검증(Validation)과 응답 형식(JSON, View)을 결정한다.
2. **Service**
   - 트랜잭션과 비즈니스 로직을 처리한다.
3. **Repository**
   - 데이터베이스에 대한 CRUD(조회/저장/수정/삭제)를 담당한다.

컨트롤러는 **가능하면 비즈니스 로직을 직접 처리하지 않고**,  
“요청을 받는다 → Service 에게 위임한다 → 결과를 응답으로 감싼다” 역할에 집중하는 것이 좋습니다.

---

### [핵심!] 🔄 컨트롤러 ↔ 서비스 연동 예시 (`/join`)

현재 프로젝트의 회원가입 흐름은 다음과 같이 이어집니다.

1. **클라이언트 요청 → DTO 매핑**
   - `UserController` 의 `join` 엔드포인트에서 JSON 바디를 `joinRequest` DTO 로 받습니다.
   - 예:

     ```java
     @PostMapping("/join")
     public String join(@RequestBody joinRequest request) {
         String id = request.getId();
         String name = request.getName();
         String email = request.getEmail();
         String password = request.getPassword();
         ...
     }
     ```

2. **서비스 호출**
   - 컨트롤러는 꺼낸 값들을 `UserService.join(...)` 에 넘겨줍니다.
   - 컨트롤러는 “어떻게 저장하는지”는 모르고, **“가입 시도” 라는 의미만 전달**합니다.

3. **서비스에서 비밀번호 해싱 + 저장**
   - `UserServiceImpl` 에서:
     - 비밀번호를 SHA-256 으로 해시
     - `Member` 엔티티를 빌더로 생성
     - `MemberRepository.save(member)` 로 DB 에 저장
   - 이 단계에서 **도메인 규칙(중복 체크, 암호화, 트랜잭션 등)을 모두 처리**합니다.

4. **컨트롤러에서 응답 생성**
   - 서비스에서 돌아온 결과 문자열에 따라 `"success!"` / `"failed!"` 와 같은 응답 메시지를 정해 클라이언트에 반환합니다.

이렇게 분리해 두면:

- 컨트롤러는 **입력 검증·형식·응답 형태** 에 집중하고,
- 서비스는 **회원가입 로직(암호화, 저장 등)** 에 집중하며,
- 레포지토리는 **DB 저장/조회** 만 신경 쓰게 되어 각 계층 책임이 명확해집니다.

---

### 정리

이 `controller` 패키지는:

- `user` 도메인과 관련된 **모든 HTTP 엔드포인트의 진입점**이고,
- 사용자의 요청을 받아 **Service/Repository 계층으로 연결해 주는 “얇은 입구”** 역할을 합니다.

앞으로 `UserController` 등에 엔드포인트를 추가할 때는,

- URL 설계, HTTP 메서드, 응답 형태(JSON 구조)를 여기에서 정의하고  
- 실제 비즈니스 규칙은 Service 로 위임하는 구조를 유지하면  
읽기 쉽고, 테스트하기 좋은 스프링 부트 애플리케이션을 만들 수 있습니다.

