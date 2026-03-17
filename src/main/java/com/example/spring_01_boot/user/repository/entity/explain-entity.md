### 📍 개요

`spring_01_boot/src/main/java/com/example/spring_01_boot/user/repository/entity` 는  
`user` 도메인에서 **DB 테이블과 매핑되는 JPA 엔티티 클래스들**이 모여 있는 패키지입니다.

이 안의 `Member` 클래스가 실제로 **H2 DB의 한 테이블(예: MEMBER)** 과 1:1로 연결되어 동작합니다.

---

### 🧱 현재 엔티티: `Member`

```java
@Entity
@Builder
@NoArgsConstructor
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long index;

    private String id;
    private String name;
    private String email;
    private String password;
}
```

각 필드의 의미를 정리하면 다음과 같습니다.

- **`index`**
  - 타입: `Long`
  - 어노테이션: `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`
  - 역할: 테이블의 **기본 키(Primary Key)**, DB에서 자동 증가하는 숫자 컬럼

- **`id`**
  - 타입: `String`
  - 의미: 화면/비즈니스에서 사용하는 **사용자 아이디(로그인 ID)** 역할을 하는 값

- **`name`**
  - 타입: `String`
  - 의미: 사용자의 실제 이름(표시용 이름)

- **`email`**
  - 타입: `String`
  - 의미: 사용자 이메일 주소

- **`password`**
  - 타입: `String`
  - 의미: 사용자 비밀번호(실서비스에서는 해시된 값 보관 권장)

Lombok 어노테이션들의 역할은 다음과 같습니다.

- `@Getter` : 모든 필드에 대한 getter 메서드를 자동 생성
- `@NoArgsConstructor` : 파라미터 없는 기본 생성자 생성 (JPA가 엔티티 생성 시 사용)
- `@AllArgsConstructor(access = AccessLevel.PRIVATE)` : 모든 필드를 받는 생성자를 **private** 으로 생성
- `@Builder` : `Member.builder()...build()` 형태로 객체를 생성할 수 있게 해 줌

이렇게 하면:

- JPA가 내부적으로 사용할 기본 생성자를 보장하면서,
- 외부에서는 **생성자 대신 빌더 패턴으로만 엔티티를 만들도록 유도**할 수 있습니다.

---

### 🧬 엔티티(Entity)란? (JPA 관점)

JPA에서 **엔티티(Entity)** 는

> “데이터베이스의 테이블과 1:1 로 매핑되는 자바 클래스”

입니다.

- `@Entity` 가 붙은 클래스 1개 → DB 테이블 1개
- 클래스의 필드들 → 테이블의 컬럼들
- `@Id` 로 표시된 필드 → 기본 키 컬럼

장점은:

- SQL을 직접 작성하지 않고도,  
  `EntityManager` 나 `Spring Data JPA` 의 `Repository` 인터페이스를 통해  
  **자바 코드만으로 CRUD를 수행**할 수 있다는 점입니다.

예를 들어 `MemberRepository` 를 `JpaRepository<Member, Long>` 으로 작성해 두면:

- `save(member)` → INSERT/UPDATE
- `findById(id)` → SELECT … WHERE index = ?
- `delete(member)` → DELETE …

등의 SQL이 자동으로 생성·실행됩니다.

---

### 🧭 ERD / O-R 매핑과의 관계

전통적인 ERD(엔터티-관계 다이어그램)에서:

- 사각형 박스 **하나가 테이블(엔터티)**,
- 그 안의 필드들이 **컬럼**을 의미합니다.

JPA 엔티티는 이 ERD 를 **자바 코드 형태로 옮겨 온 것**이라고 보면 됩니다.

- ERD 상의 `MEMBER` 테이블 ↔ 자바의 `Member` 엔티티
- PK/UK, 컬럼 타입, 제약 조건 등은 엔티티의 필드/어노테이션으로 표현

이를 흔히 **O-R 매핑(Object–Relational Mapping)** 이라고 부르며,
JPA가 이 매핑 정보를 바탕으로 SQL을 자동 생성해 줍니다.

---

### 3. 스프링 입장에서의 차이

스프링 부트 기준으로는 **DB 종류가 달라져도 엔티티/리포지토리 코드는 거의 그대로** 사용할 수 있습니다.

- 코드 레벨:
  - `@Entity`, `@Repository`, `JpaRepository` 같은 애노테이션/인터페이스는
  - H2, MySQL, Oracle, PostgreSQL 등 **어떤 RDB를 쓰더라도 동일한 방식으로 작성**합니다.

- 설정 레벨:
  - 실제로 바뀌는 것은 주로 `application.yml` 의 다음 부분입니다.
    - `spring.datasource.url` (JDBC URL)
    - `spring.datasource.driver-class-name` (JDBC 드라이버 클래스)
    - 필요하다면 `spring.jpa.database-platform` (Hibernate dialect)
  - 예를 들어,
    - 개발용 H2: `jdbc:h2:mem:testdb`
    - 운영용 MySQL: `jdbc:mysql://host:3306/dbname`
    - 운영용 Oracle: `jdbc:oracle:thin:@host:1521/servicename`
  - 이렇게 **설정만 교체**하면, 나머지 JPA 엔티티/리포지토리 코드는 그대로 재사용할 수 있습니다.

---

### ✅ 정리

- 이 패키지의 엔티티들은 **DB 구조(테이블/컬럼)를 자바 클래스와 필드로 표현한 것**입니다.
- `Member` 엔티티는 `user` 도메인의 “회원” 정보를 담당하며,
  - PK: `index`
  - 핵심 속성: `id`, `name`, `email`, `password`
- 이후 `Repository`, `Service` 계층에서 이 엔티티를 이용해  
  회원 관련 조회/등록/수정/삭제 로직을 구현하게 됩니다.
