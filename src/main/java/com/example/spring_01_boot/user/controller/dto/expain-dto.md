### 📍 개요

`spring_01_boot/src/main/java/com/example/spring_01_boot/user/controller/dto` 는  
컨트롤러에서 사용할 **요청/응답 전용 데이터 전달 객체(DTO)** 들을 모아두는 패키지입니다.

여기 있는 클래스들은 주로:

- HTTP 요청 바디(JSON)를 자바 객체로 받거나
- 서비스/엔티티에서 나온 결과를 클라이언트로 돌려줄 때

그 **형태·필드 구조만을 표현하는 역할**을 합니다.

---

### 🧾 DTO란?

DTO(Data Transfer Object)는 말 그대로 **계층 간 데이터 전달만을 위한 객체**입니다.

특징:

- **순수 데이터 홀더**: 필드 + getter/setter 위주, 비즈니스 로직 최소화
- 컨트롤러, 서비스, 외부 API 사이에서 **입·출력 형태를 명확하게 분리**하는 데 사용
- 도메인 엔티티(`Member`)와 달리,  
  API 요구사항에 맞게 필드 구성이 자유롭고 변동에도 유연합니다.

이 프로젝트에서는 컨트롤러 패키지 아래 `dto` 폴더에 두어,  
“웹 요청/응답에서 쓰는 형태”와 “도메인/엔티티”를 분리해 두었습니다.

---

### ✉️ 현재 DTO: `joinRequest`

```java
@Data
public class joinRequest {
    private String id;
    private String name;
    private String email;
    private String password;
}
```

각 필드의 의미:

- `id`       : 회원 가입 시 사용할 **로그인 ID**
- `name`     : 사용자 이름(표시용)
- `email`    : 사용자 이메일
- `password` : 평문 비밀번호 (서비스 계층에서 해싱 후 저장)

`@Data` (Lombok) 덕분에 컴파일 시 자동으로:

- getter/setter
- `toString`
- `equals` / `hashCode`

등이 생성되어, 컨트롤러 코드에서 `request.getId()`, `request.getEmail()` 처럼 바로 사용할 수 있습니다.

컨트롤러에서의 사용 예:

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

---

### ✅ 정리

- `dto` 패키지는 **HTTP 요청/응답의 형태를 정의하는 전용 객체**를 모아두는 곳입니다.
- `joinRequest` 는 회원 가입 API를 위한 입력 DTO로,
  - 클라이언트가 보내는 JSON 구조를 자바 객체로 받기 위한 용도이고
  - 실제 저장/비즈니스 로직은 서비스(`UserService`)와 엔티티(`Member`)에서 처리합니다.

