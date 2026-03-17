### 📍 개요

현재 위치인  
`spring_01_boot/src/main/resources/application.yml` 은  
스프링 부트 애플리케이션의 **환경 설정과 공통 설정값을 정의하는 파일**입니다.

프로젝트 이름, 데이터베이스 연결 정보, 포트, 로그 레벨 등  
애플리케이션이 동작하는 데 필요한 **각종 설정을 중앙화해서 관리**합니다.

---

### 🧾 `application.yml` (또는 `application.properties`) 란?

스프링 부트는 기본적으로 다음 두 가지 형식의 설정 파일을 지원합니다.

- `application.properties` : `key=value` 형태
- `application.yml`        : 들여쓰기를 사용하는 **YAML 계층 구조** 형태

두 파일 모두 역할은 같고, **표현 방식만 다릅니다.**  
이 프로젝트에서는 **가독성이 좋은 `application.yml`** 을 사용하고 있습니다.

스프링 부트는 실행 시 `src/main/resources` 경로에서  
`application.yml` / `application.properties` 를 자동으로 찾아 읽어옵니다.

---

### ⚙️ 현재 `application.yml` 에서 설정한 내용

```yaml
spring:
  application:
    name: spring-01-boot

  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true
      path: /h2-console
```

각 항목의 의미는 다음과 같습니다.

- **`spring.application.name`**
  - 애플리케이션 이름
  - 로그나 모니터링 툴에서 서비스 이름으로 사용될 수 있습니다.

- **`spring.datasource.*`**
  - 스프링이 사용할 **데이터베이스 연결 정보**
  - 여기서는 H2 인메모리 DB `jdbc:h2:mem:testdb` 를 사용
  - `username=sa`, `password` 는 비어 있음(기본 계정)

- **`spring.h2.console.*`**
  - H2 데이터베이스 웹 콘솔 설정
  - `enabled: true` → 콘솔 기능 활성화
  - `path: /h2-console` → 브라우저에서 `http://localhost:8080/h2-console` 로 접속 가능

---

### 🧠 정리

- `application.yml` 은 **스프링 부트 설정의 중심 파일**입니다.
- 데이터소스, 포트, 로그, 보안, 프로파일(dev/prod) 등을 **코드와 분리해서 관리**할 수 있습니다.
- 현재 이 프로젝트에서는:
  - **애플리케이션 이름**
  - **H2 인메모리 데이터베이스 연결**
  - **H2 콘솔 접속 경로**
  
를 설정해 두었고,  
앞으로 학습을 진행하면서 **필요한 설정들을 이 파일에 단계적으로 추가**해 나가면 됩니다.
