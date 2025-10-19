# User Auth Service

사용자 인증 및 관리를 위한 RESTful API 서비스입니다. JWT 기반 인증, 토큰 갱신, 사용자 CRUD, 비동기 이벤트 처리 등의 기능을 제공합니다.

---

## 프로젝트 실행 방법

### Docker Compose를 이용한 실행

1. **프로젝트 클론**
   ```bash
   git clone <repository-url>
   cd user-auth-service
   ```

2. **Docker Compose로 전체 스택 실행**
   ```bash
   docker-compose up -d
   ```

   이 명령어는 다음 서비스들을 실행합니다:
    - **user-auth-service**: 애플리케이션 (Port 8000)
    - **postgres**: PostgreSQL 17 데이터베이스 (Port 5432)
    - **rabbitmq**: RabbitMQ 4.1 with Management (Port 5672, 15672)

3. **서비스 상태 확인**
   ```bash
   docker-compose ps
   ```

4. **애플리케이션 접속**
    - API 엔드포인트: `http://localhost:8000/api`
    - Swagger UI: `http://localhost:8000/api/swagger-ui/index.html`
    - RabbitMQ Management: `http://localhost:15672` (guest/guest)

5. **서비스 종료**
   ```bash
   docker-compose down
   ```

### 로컬 개발 환경 실행

1. **PostgreSQL 및 RabbitMQ 실행**
   ```bash
   docker-compose up -d postgres rabbitmq
   ```

2. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

3. **테스트 실행**
   ```bash
   # 전체 테스트
   ./gradlew test
   ```

4. **코드 품질 검사 (ktlint)**
   ```bash
   ./gradlew ktlintCheck
   ./gradlew ktlintFormat
   ```

### 초기 데이터

초기 마이그레이션 시 관리자 계정이 자동으로 생성됩니다:

- **Email**: `admin@example.com`
- **Password**: `secure_password`
- **Role**: `ADMIN`

---

## 사용된 기술 스택

### Backend Framework

- **Kotlin**: 2.2.20
- **Spring Boot**: 3.5.6
    - Spring Web
    - Spring Data JPA
    - Spring Security
    - Spring Validation
    - Spring AMQP (RabbitMQ)
- **Java**: 21 (LTS)

### Database & Migration

- **PostgreSQL**: 17
- **Flyway**: 11.14.1 - 데이터베이스 마이그레이션 관리

### Message Broker

- **RabbitMQ**: 4.1 with Management Plugin

### Security & Authentication

- **JJWT**: 0.13.0 - JWT 토큰 생성 및 검증
- **BCrypt**: Spring Security에 내장된 비밀번호 암호화

### ORM & Database Tools

- **Hibernate**: 6.3 (Spring Boot 3.5.6에 포함)
- **Hypersistence Utils**: 3.11.0 - Hibernate 성능 최적화 및 유틸리티
- **HikariCP**: Connection Pool (Spring Boot 기본)

### ID Generation

- **Yitter Snowflake ID Generator**: 1.0.6 - 분산 환경에서 고유 ID 생성

### Documentation

- **SpringDoc OpenAPI**: 2.8.6 - Swagger UI

### Testing

- **Kotest**: 6.0.4 - Kotlin 친화적 테스트 프레임워크
- **MockK**: 1.14.6 - Kotlin 전용 모킹 라이브러리
- **TestContainers**: 1.21.3 - PostgreSQL, RabbitMQ 컨테이너 기반 통합 테스트
- **Spring Boot Test**: 3.5.6

### Logging

- **Kotlin Logging**: 7.0.13 - Kotlin 친화적 로깅 라이브러리

### Build & Code Quality

- **Gradle**: 8.x (Kotlin DSL)
- **KtLint**: 13.1.0 - Kotlin 코드 스타일 검사

### Infrastructure

- **Docker & Docker Compose**: 컨테이너화 및 오케스트레이션

---

## 설계 결정 이유

### 1. JWT 인증 방식

**Access Token + Refresh Token 패턴**

```yaml
jwt:
  access-token-expiration: 900000     # 15분
  refresh-token-expiration: 604800000 # 7일
```

**선택 이유:**

- **Stateless 인증**: 서버 메모리에 세션을 저장하지 않아 horizontal scaling 에 유리
- **짧은 Access Token 수명**: 토큰 탈취 시 피해를 최소화 (15분)
- **Refresh Token Rotation**: Auth0의 Refresh Token Rotation 패턴 구현
    - Token Family ID를 통한 토큰 재사용 공격 탐지
    - 재사용 탐지 시 해당 family의 모든 토큰 무효화
    - 정당한 사용자도 재인증 필요 (보안 강화)

### 2. Snowflake ID 생성 전략

**Yitter Snowflake ID Generator 사용**

**선택 이유:**

- **분산 환경 고려**: Auto Increment PK는 분산 환경에서 충돌 가능
- **고성능**: UUID보다 작은 크기(Long)로 인덱스 성능 향상
- **시간 정렬 가능**: ID 자체에 타임스탬프 정보 포함

### 3. Soft Delete 전략

**`deleted_at` 필드를 이용한 소프트 삭제**

```kotlin
@Entity
@SQLRestriction("deleted_at IS NULL")
class User {
    var deletedAt: LocalDateTime? = null

    fun delete() {
        this.deletedAt = LocalDateTime.now()
    }
}
```

**선택 이유:**

- **감사(Audit) 추적**: 삭제된 데이터의 이력 보존
- **참조 무결성 유지**: Foreign Key 제약 조건 유지
- **Hibernate @SQLRestriction**: 모든 쿼리에 자동으로 `deleted_at IS NULL` 조건 추가

### 4. 비밀번호 암호화: BCrypt

**Spring Security의 BCryptPasswordEncoder 사용**

**선택 이유:**

- **솔트(Salt) 자동 생성**: Rainbow Table 공격 방어
- **적응형 해싱**: Cost Factor 조정으로 미래의 컴퓨팅 성능 증가에 대응
- **업계 표준**: OWASP 권장 알고리즘

```kotlin
@Bean
fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
```

### 5. 데이터베이스 스키마 설계

**Users 테이블**

```sql
CREATE TABLE users
(
    id         BIGINT PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(100) NOT NULL,
    role       VARCHAR(100) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    deleted_at TIMESTAMP NULL
);

-- 성능 최적화 인덱스
CREATE INDEX idx_users_active ON users (id) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX idx_users_email_active ON users (email) WHERE deleted_at IS NULL;
```

**Refresh Tokens 테이블**

```sql
CREATE TABLE refresh_tokens
(
    id         BIGINT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(512) NOT NULL UNIQUE,
    family_id  BIGINT       NOT NULL, -- Token Family 추적
    is_revoked BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL
);

-- 조회 성능 최적화 인덱스
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_family_id ON refresh_tokens (family_id);
```

**설계 결정:**

- **Partial Unique Index**: Soft Delete된 사용자의 이메일 재사용 가능
- **Token Family Index**: 재사용 탐지 시 빠른 전체 패밀리 무효화

### 6. 인가(Authorization) 전략

**Spring Security의 `@PreAuthorize` 사용**

```kotlin
@PreAuthorize("hasRole('ADMIN') or (hasRole('MEMBER') and #id == authentication.principal.userId)")
fun getById(@PathVariable id: Long): ResponseEntity<UserResponse>
```

**선택 이유:**

- **세밀한 권한 제어**: 메소드 레벨에서 복잡한 권한 로직 표현
- **SpEL 활용**: 동적 권한 검증 (자신의 정보만 조회/수정)
- **선언적 보안**: 비즈니스 로직과 보안 로직 분리

**권한 정책:**

- `ADMIN`: 모든 사용자 정보 조회/수정/삭제 가능
- `MEMBER`: 자신의 정보만 조회/수정/삭제 가능

---

## 문제 해결 과정 및 고민

### 1. Refresh Token 재사용 공격 방어 설계

**문제:**
Refresh Token이 탈취되었을 때 어떻게 탐지하고 방어할 것인가?

**시나리오:**

1. 정상 클라이언트가 `refreshToken1` 보유
2. 공격자가 `refreshToken1` 탈취
3. 정상 클라이언트가 `refreshToken1`로 새 토큰 요청 → `refreshToken2` 발급
4. 공격자가 `refreshToken1` 재사용 시도

**구현한 해결책 ([Auth0 패턴](https://auth0.com/docs/secure/tokens/refresh-tokens/refresh-token-rotation)):**

```kotlin
@Service
class RefreshTokenService {
    fun rotateRefreshToken(oldToken: String): RotationResult {
        val oldRefreshToken = findByToken(oldToken)

        ...

        // 이미 무효화된 토큰 사용 시도 = 재사용 공격 탐지
        if (oldRefreshToken.isRevoked) {
            revokeTokenFamily(token.familyId)  // 전체 패밀리 무효화
            throw InvalidTokenException(...)
        }

        ...

        oldRefreshToken.revoke()  // 현재 토큰 무효화
    }
}
```

### 2. RabbitMQ 메시지 전송 확인 (Functional Test)

**문제:**
사용자 삭제 API 호출 후 RabbitMQ Consumer가 실제로 메시지를 받아 로그를 출력하는지 검증하고 싶었습니다.

**시도한 방법들:**

1. **Log Appender 추가** - 단위 테스트에서는 접근 가능했지만 기능 테스트 환경에서 Logger 인스턴스 접근 어려움
2. **RabbitMQ Queue 직접 조회** - Consumer가 이미 메시지를 소비하여 큐가 비어있음

**최종 결정:**

- Functional Test에서는 **API 응답 및 DB 에 적용됨 검증** (204 No Content)
- Consumer 로직은 **별도 Unit Test로 검증**

---

## 비동기 처리 설명

### 구현 개요

사용자 탈퇴 시 발생하는 부가 작업(이메일 발송, 파일 삭제)을 **RabbitMQ 메시지 큐**를 이용해 비동기로 처리합니다.

### 아키텍처

```
  [UserService.deleteById()]
              ↓
        publish event
              ↓
[UserEventListener] (after commit)
              ↓
RabbitMQ Exchange (user.events.fanout)
              ↓
   ┌─────────────────────┐
   ↓                     ↓
Email Queue         Files Queue
   ↓                     ↓
EmailConsumer       FileConsumer
   ↓                     ↓
Send Email          Delete Files
```

### 기술 선택 이유

**1. RabbitMQ를 선택한 이유**

- **신뢰성**: 메시지 영속성 보장 (Durable Queue, Persistent Message)
- **유연한 라우팅**: Exchange-Queue 바인딩으로 다양한 라우팅 패턴 구현
- **재시도 메커니즘**: DLQ(Dead Letter Queue)와 함께 실패 처리 전략 수립 가능
- **확장성**: Consumer를 독립적으로 스케일 아웃 가능

**2. Fanout Exchange를 선택한 이유**

```kotlin
@Bean
fun userEventsExchange() = FanoutExchange(USER_EVENTS_EXCHANGE)
```

- **확장 용이**: 새로운 큐 추가 시 기존 코드 수정 불필요
- **다중 Consumer**: 하나의 이벤트를 여러 Consumer가 동시에 처리
- **관심사 분리**: 각 Consumer가 독립적으로 작업 수행

**3. Spring의 TransactionalEventListener를 선택한 이유**

```kotlin
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
fun handle(event: UserDeletedEvent) {
    rabbitTemplate.convertAndSend(USER_EVENTS_EXCHANGE, "", event)
}
```

- **트랜잭션 일관성**: DB 커밋 실패 시 이벤트 발행 안 함
- **데이터 정합성**: 사용자 삭제가 완전히 완료된 후에만 후속 작업 트리거
- **At-Least-Once 보장**: 커밋 후 발행하여 메시지 손실 최소화

### 재시도 및 에러 핸들링

**재시도 설정 (application.yml):**

```yaml
spring:
  rabbitmq:
    listener:
      simple:
        retry:
          enabled: true
          initial-interval: 3000 # 첫 재시도: 3초 후
          max-attempts: 3        # 최대 3회 시도
          multiplier: 2          # 지수 백오프 (3s → 6s → 12s)
```

**Dead Letter Queue (DLQ):**

- 3회 재시도 실패 시 메시지가 DLQ로 이동
- DLQ 메시지는 수동으로 검토 및 재처리
- RabbitMQ Management UI에서 확인 가능

---

## API 문서

### Swagger UI

프로젝트는 SpringDoc OpenAPI를 이용해 자동으로 API 문서를 생성합니다.

**접속 URL:**

```
http://localhost:8000/api/swagger-ui/index.html
```

### 주요 엔드포인트

#### Authentication

| Method | Endpoint                  | Description | Auth Required |
|--------|---------------------------|-------------|---------------|
| POST   | `/api/auth/signup`        | 회원가입        | No            |
| POST   | `/api/auth/signin`        | 로그인         | No            |
| POST   | `/api/auth/refresh-token` | 토큰 갱신       | No            |

#### User Management

| Method | Endpoint          | Description          | Auth Required |
|--------|-------------------|----------------------|---------------|
| GET    | `/api/users`      | 사용자 목록 조회 (페이징, 필터링) | ADMIN         |
| GET    | `/api/users/{id}` | 사용자 상세 조회            | ADMIN or Own  |
| PUT    | `/api/users/{id}` | 사용자 정보 수정            | ADMIN or Own  |
| DELETE | `/api/users/{id}` | 사용자 삭제 (Soft Delete) | ADMIN or Own  |

---

## 테스트 전략

### 테스트 범위

**단위 테스트 (Unit Tests)**

- **도메인 로직**: `User`, `RefreshToken` 엔티티 메소드
- **서비스 레이어**: `UserService`, `RefreshTokenService`, `AuthFacade`, `UserFacade`
- **Consumer**: `UserDeletionEmailConsumer`, `UserDeletionFileConsumer`
- **Event Listener**: `UserEventListener`
- **유틸리티**: `JwtTokenProcessor`, `Functions` (이메일 검증 등)

**특징:**

- MockK를 이용한 의존성 모킹
- 비즈니스 로직의 다양한 엣지 케이스 검증
- 메시지 전송/수신 검증

**통합 테스트 (Integration Tests)**

- **Repository**: `UserRepository`, `RefreshTokenRepository` (TestContainers 사용)

**특징:**

- PostgreSQL 컨테이너와 통합
- 데이터베이스 쿼리, 트랜잭션 동작 검증

**기능 테스트 (Functional Tests / E2E)**

- **Controller**: `AuthController`, `UserController`
- HTTP 요청-응답 전체 플로우 검증

**특징:**

- `@SpringBootTest`로 전체 애플리케이션 컨텍스트 로드
- `TestRestTemplate`을 이용한 실제 HTTP 요청
- TestContainers로 실제 DB, RabbitMQ 사용
- JWT 토큰 생성 및 인증 플로우 검증

### 주요 테스트 시나리오

**인증/인가 시나리오:**

- 회원가입 성공/실패 (이메일 중복, 비밀번호 길이 부족)
- 로그인 성공/실패 (잘못된 비밀번호, 존재하지 않는 이메일)
- Access Token 생성 및 검증
- Refresh Token 갱신 성공
- 만료된 Refresh Token 사용 실패
- 무효화된 Refresh Token 재사용 시 패밀리 무효화
- 유효하지 않은 JWT 형식 거부

**사용자 관리 시나리오:**

- ADMIN - 모든 사용자 조회/수정/삭제 가능
- MEMBER - 자신의 정보만 조회/수정/삭제 가능
- MEMBER - 다른 사용자 정보 접근 시 403 Forbidden
- 페이징 및 필터링 (이름, 이메일 검색)
- 사용자 정보 수정 후 DB 반영 확인
- Soft Delete 확인 (deleted_at 설정, 재조회 시 404)
- 이메일 중복 검증 (자기 자신 제외)

**비동기 처리 시나리오:**

- 사용자 삭제 시 `UserDeletedEvent` 발행
- TransactionalEventListener가 커밋 후 RabbitMQ에 전송
- EmailConsumer가 메시지 수신 및 로깅
- FileConsumer가 메시지 수신 및 로깅

### Test Helper

**TestHelper**를 통해 테스트 데이터 생성 및 관리:

```kotlin
  @Component
class TestHelper(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProcessor: JwtTokenProcessor,
) {
    fun createUser(
        name: String = generateString(),
        email: String = "${generateString()}@example.com",
        password: String = generateString(),
        role: Role = Role.MEMBER
    ): User

    fun createRefreshToken(userId: Long): RefreshToken

    fun generateToken(user: User): String

    fun getUser(id: Long): User

    ...
}
```

---

## AI 사용 기록

이 프로젝트는 **Claude Code** AI 어시스턴트를 활용하여 개발되었습니다. 아래는 AI를 사용한 주요 지점과 프롬프트입니다.

### 1. JWT 인증 구현

**사용 지점:** JWT 토큰 생성, 검증 로직 및 Spring Security 통합
**프롬프트:**

- "I am now going to implement /signin API in AuthController. I want you to start by adding JWT configuration properties
  to application.yml. And then create JwtProperties, add @EnableConfigurationProperties for JwtProperties in
  Application.kt, and implement JwtTokenProcessor that generates access and refresh token inside /configuration/security
  package"

**결과:**

- `JwtTokenProcessor` 클래스 생성
- `JwtProperties` 설정 클래스 생성
- Access Token/Refresh Token 생성 로직 구현

### 2. Swagger UI 통합 및 API 문서화

**사용 지점:** OpenAPI 문서 자동 생성
**프롬프트:**

- "Add swagger UI to expose documentation for APIs - add the dependency to build.gradle and enable swagger UI. The
  endpoints need to be exposed in WebSecurityConfig"
- "@Operation and @ApiResponses help the swagger UI, but it really makes this controller code ugly. Refactor out the
  annotations to a docs package"

**결과:**

- SpringDoc OpenAPI 의존성 추가
- Custom Annotation (`@PostSignUp`, `@GetUserById` 등) 생성하여 코드 정리

### 3. 테스트 코드 작성

**사용 지점:** Controller, Service, Repository 계층 테스트
**프롬프트 예시:**

- "Implement the test code for API endpoint signIn"
- "Implement test code for `getUsersByPage`. Mock the return value and use verify"
- "Implement test code for `rotateRefreshToken`"

**결과:**

- Unit/Integration/Functional 테스트 분리
- TestHelper 유틸리티 클래스 생성
- 주요 시나리오 커버리지 확보

### 4. README 작성

**사용 지점:** README 작성
**프롬프트 예시:**

- "I have a draft of the README. I want you to clean it up by separating into logical sections and simplifying
  unnecessary parts and summarize things into bullet-points. Fix my messy grammar"

**결과:**

- README 정리
