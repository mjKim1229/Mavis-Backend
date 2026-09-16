# mavis-backend

Mavis 이커머스 백엔드. Java 21 / Spring Boot 3.5 / JPA + QueryDSL / MySQL, Gradle 멀티 모듈.
모듈: `Mavis-Api`(사용자 API, 8080) · `Mavis-Admin`(어드민 API, 8081) · `Mavis-Domain` · `Mavis-Infrastructure` · `Mavis-Common` — 각 모듈 규칙은 모듈 내 `AGENTS.md`.

## Module Dependency Structure

```
Mavis-Api / Mavis-Admin  → Domain, Infrastructure, Common
Mavis-Domain             → Infrastructure, Common
Mavis-Infrastructure     → Common
Mavis-Common             → (없음, 최하위)
```

- Domain → Infrastructure 의존은 `PaymentMethod`가 `TossPaymentMethod`를 참조하는 1건뿐. 신규 참조 추가 금지
- 역방향 참조(Infrastructure/Common → Domain, Domain → Api/Admin) 금지 — 순환 의존

## For AI Agents

### Build & Run Commands

```bash
./gradlew build                      # 전체 빌드
./gradlew clean build                # 클린 빌드
./gradlew :Mavis-Api:test            # 모듈 단위 테스트
./gradlew :Mavis-Domain:test         # 도메인 통합 테스트 (Testcontainers + MySQL)
./gradlew :Mavis-Api:bootRun         # 사용자 API 실행 (port 8080)
./gradlew :Mavis-Admin:bootRun       # 어드민 API 실행 (port 8081)
```

### Common Patterns
- **예외 처리:** 모든 에러는 `MavisException(ErrorCode)` 패턴. `ErrorCode` enum은 도메인 패키지별로 정의
- **리포지토리:** 복잡한 쿼리는 `*RepositoryCustom` 인터페이스 + `*RepositoryImpl` (JPAQueryFactory). Q클래스는 `MavisApiServerApplication` 시작 시 `Class.forName`으로 eager load. 단순 존재 여부 확인은 `findBy().isPresent()` 대신 `existsBy()` 사용 — 불필요한 엔티티 로딩 금지.
- **외부 API 에러:** Feign 에러 디코더 (예: `TossPaymentsErrorDecoder`)가 외부 HTTP 에러를 `MavisException`으로 변환
- **민감 설정:** DB 자격증명, OAuth 키, TossPayments 키, 메일 비밀번호는 AWS Parameter Store로 주입 — `application.yml`에 직접 기입 금지
- **트랜잭션:** 조회 전용 서비스 메서드는 `@Transactional(readOnly = true)` 사용 — dirty checking 비활성화로 성능 향상
- **이벤트:** 이메일 발송 등 사이드 이펙트 이벤트 리스너는 `@TransactionalEventListener(phase = AFTER_COMMIT)` 사용 — DB 커밋 실패 시 외부 호출 방지

## 코드 스타일 규약

- 메서드 호출 중첩 금지. 중간 결과는 변수로 먼저 받을 것.
- `var` 사용 금지. 모든 변수 선언은 명시적 타입으로 작성.

## DTO 네이밍 규약

| 종류 | Suffix | 위치 | 설명 |
|------|--------|------|------|
| QueryDSL 프로젝션 결과 | `*Row` | `Mavis-Domain/.../dto/` | DB에서 조회한 raw 데이터. `Projections.constructor`로 직접 매핑 |
| API 응답 | `*Response` | `Mavis-Api/.../dto/` | 클라이언트에 전달되는 최종 응답 객체 |
| API 요청 | `*Request` | `Mavis-Api/.../dto/` | 클라이언트로부터 받는 입력 객체 |

**예시:**
- `ProductInquiryRow` — 문의 + 답변을 단일 쿼리로 조회한 프로젝션 (Domain)
- `GetProductInquiryResponse` — 클라이언트에 내보내는 문의 응답 (Api)

## 서비스 레이어 아키텍처 규약

### 레이어 구조

```
Controller
    ↓
Service (비즈니스 로직)
    ↓
Repository
```

### 규칙

**1. Service 간 참조 허용**

같은 Service 계층끼리 참조 가능. 순환 참조 금지 (`A → B → A`).

**2. 도메인 로직 위치**

규칙/불변식은 Entity에 우선 배치.
Entity가 담기 어려운 흐름 조합은 Service에서 처리.

**3. Facade 사용 기준**

Facade 사용: (1) 여러 Service 조합, 또는 (2) 외부 API 호출(결제·OAuth 등) 포함 — 외부 호출은 트랜잭션 밖(Facade)에서 수행.
그 외 단일 Service로 끝나는 흐름은 Facade 만들지 않음.
