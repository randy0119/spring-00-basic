### 📍 개요

`spring_01_boot/src/main/java/com/example/spring_01_boot/user/repository` 는  
`user` 도메인에서 **데이터베이스와 직접 대화하는 레이어(Repository)** 가 모여 있는 패키지입니다.

엔티티(`Member`) 가 **“무엇을 저장할지”** 를 정의한다면,  
레포지토리는 **“어떻게 저장/조회할지”** 를 담당합니다.

---

### 🗂 레포지토리(Repository)란?

스프링 데이터 JPA 기준에서 **레포지토리 인터페이스**는:

> JPA 엔티티에 대해 “조회·저장·수정·삭제” 를 수행하는 **데이터 접근 계층(DAO)** 을  
> 인터페이스만으로 선언하는 방법

입니다.

예: 현재 프로젝트의 `MemberRepository`

```java
public interface MemberRepository extends JpaRepository<Member, Long> {
}
```

여기서:

- `Member` : 어떤 엔티티를 다룰지 (도메인 타입)
- `Long`   : 그 엔티티의 PK 타입 (`Member.index`)

을 의미합니다.

`JpaRepository` 를 상속하면, 아래와 같은 메서드를 **직접 구현 없이 바로 사용**할 수 있습니다.

- `save(entity)` : INSERT 또는 UPDATE
- `findById(id)` : PK 기반 단건 조회
- `findAll()` : 전체 목록 조회
- `delete(entity)` / `deleteById(id)` : 삭제

또한, 메서드 이름 규칙을 이용하면 **쿼리 메서드**도 쉽게 정의할 수 있습니다.

```java
Optional<Member> findById(String id);
List<Member> findByNameContaining(String keyword);
```

이런 메서드를 인터페이스에 선언만 해 두면,

- `findById` → `where id = ?`
- `findByNameContaining` → `where name like %?%`

형태의 SQL을 스프링 데이터 JPA가 자동으로 만들어 실행합니다.

---

### 🧩 이 패키지와 다른 계층의 관계

`user` 도메인을 예로 들면, 전형적인 흐름은 다음과 같습니다.

1. **Controller (`UserController`)**
   - HTTP 요청을 받는다. (예: `GET /members`, `POST /members`)
   - 요청 파라미터/바디를 DTO로 변환한다.
   - Service 를 호출한다.

2. **Service (`UserService`)**
   - 비즈니스 규칙을 처리한다. (중복 체크, 권한 검증, 트랜잭션 등)
   - 필요 시 Repository 를 여러 번 호출해 데이터를 조합/검증한다.

3. **Repository (`MemberRepository`)**
   - 실제로 DB에 쿼리를 날려 **Member 엔티티를 저장/조회/삭제**한다.

이렇게 해서 Controller 는 DB를 직접 몰라도 되고,  
**Service ↔ Repository 사이에 역할이 분리**되면서 코드가 깔끔해집니다.

---

### 🧪 어떻게 테스트 하나요?

현재 프로젝트에는 `MemberRepository` 에 대한 테스트 클래스가 이미 준비되어 있습니다.

```java
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    // 예시: CRUD 동작을 검증하는 테스트 메서드
    // @Test
    // void crudTest() { ... }
}
```

이런 식의 테스트를 작성/실행하면:

- 실제 H2 인메모리 DB(`jdbc:h2:mem:testdb`) 에 테이블이 생성되고,
- `save`, `findById`, `findAll` 등의 동작이 기대대로 되는지 검증할 수 있습니다.

**실행 방법(요약)**:

- IDE에서: `MemberRepositoryTest` 클래스(또는 메서드) 옆의 ▶ 버튼 클릭
- 터미널에서: 프로젝트 루트(`spring_01_boot`)에서

  ```bash
  ./mvnw test
  ```

을 실행하면 `src/test/java` 아래의 모든 테스트가 함께 실행됩니다.

---

### ✅ 정리

- 이 패키지의 레포지토리들은 **엔티티와 DB 사이의 다리 역할**을 합니다.
- `MemberRepository` 는 `Member` 엔티티를 **저장/조회/삭제**하는 전담 인터페이스입니다.
- 스프링 데이터 JPA 덕분에, 복잡한 SQL을 직접 쓰지 않아도  
  인터페이스 선언만으로 대부분의 CRUD 기능을 구현할 수 있고,  
  필요한 경우 테스트 코드(`MemberRepositoryTest`)를 통해 동작을 손쉽게 검증할 수 있습니다.