<!-- Generated: 2026-04-18 | Updated: 2026-04-18 -->

# mavis-backend

## Purpose
Mavis 이커머스 플랫폼의 백엔드 서버. Gradle 멀티 모듈 아키텍처로 구성된 Spring Boot 애플리케이션으로,
사용자 API, 어드민 API, 도메인 모델, 공통 유틸리티, 외부 인프라 연동을 분리하여 관리한다.

## Key Files

| File | Description |
|------|-------------|
| `build.gradle` | 루트 Gradle 빌드 설정 |
| `settings.gradle` | 멀티 프로젝트 모듈 등록 |
| `docker-compose.yml` | 프로덕션 Docker 구성 |
| `docker-compose.local.yml` | 로컬 개발용 Docker 구성 |
| `CLAUDE.md` | Claude Code 작업 지침 |
| `discord_send.ps1` | Discord 알림 전송 PowerShell 스크립트 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `Mavis-Api/` | 사용자용 REST API 서버 (see `Mavis-Api/AGENTS.md`) |
| `Mavis-Admin/` | 어드민 대시보드 API 서버 (see `Mavis-Admin/AGENTS.md`) |
| `Mavis-Common/` | 공통 DTO, 유틸리티, 예외 정의 (see `Mavis-Common/AGENTS.md`) |
| `Mavis-Domain/` | 도메인 엔티티, 리포지토리, 비즈니스 로직 (see `Mavis-Domain/AGENTS.md`) |
| `Mavis-Infrastructure/` | 외부 서비스 연동 (OAuth, 결제, S3, 이메일, Discord) (see `Mavis-Infrastructure/AGENTS.md`) |
| `.github/workflows/` | CI/CD 파이프라인 (GitHub Actions) |
| `.claude/` | Claude Code 설정 및 커스텀 커맨드/스킬 |

## Module Dependency Structure

```
Mavis-Api / Mavis-Admin  (Application Layer)
         ↓
    Mavis-Common         (Shared: DTOs, Exceptions, JWT, Utils)
         ↓
    Mavis-Domain         (Domain Models, Repositories, Business Logic)
         ↓
Mavis-Infrastructure     (External: OAuth, TossPayments, S3, Email, Discord)
```

## For AI Agents

### Working In This Directory
- 루트 레벨 파일(build.gradle, docker-compose 등)은 전체 빌드에 영향을 주므로 신중하게 수정
- 새 모듈 추가 시 `settings.gradle`에 등록 필요
- 민감 설정(DB 자격증명, OAuth 키, 메일 비밀번호 등)은 AWS Parameter Store로 주입 — `application.yml`에 직접 기입 금지

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
- **리포지토리:** 복잡한 쿼리는 `*RepositoryCustom` 인터페이스 + `*RepositoryImpl` (JPAQueryFactory). Q클래스는 `MavisApiServerApplication` 시작 시 `Class.forName`으로 eager load
- **서비스 분리:** `*Reader` / `*Appender`·`*Modifier` 는 의미있는 도메인 로직이 있을 때만 생성. 단순 Repository 위임이면 Service에서 직접 호출. / `*Facade` 는 다중 Service 조합 또는 외부 API 호출 포함 시 사용
- **외부 API 에러:** Feign 에러 디코더 (예: `TossPaymentsErrorDecoder`)가 외부 HTTP 에러를 `MavisException`으로 변환
- **민감 설정:** DB 자격증명, OAuth 키, TossPayments 키, 메일 비밀번호는 AWS Parameter Store로 주입 — `application.yml`에 직접 기입 금지

### Tech Stack

| 분류 | 기술 | 버전 |
|------|------|------|
| Framework | Spring Boot | 3.5.4 |
| JVM | Java | 21 |
| Build | Gradle | 8.14.3 |
| ORM | JPA/Hibernate + QueryDSL | 5.0.0 |
| Auth | JJWT + Spring Security | 0.12.6 |
| HTTP Client | Spring Cloud OpenFeign | 2025.0.0 |
| Storage | AWS S3 SDK | 2.29.50 |
| Testing | JUnit + Testcontainers | 1.20.4 |
| DB | MySQL | — |

<!-- MANUAL: -->

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

**2. Reader / Appender 사용 기준**

| 케이스 | 처리 |
|--------|------|
| Repository 얇게 감싸기만 함 | 삭제 → Service에서 Repository 직접 호출 |
| 의미있는 도메인 로직 포함 | Service 계층 내부 컴포넌트로 유지 |

Reader/Appender는 별도 계층이 아님. Service 계층 내부 협력 객체.
Controller 등 외부에서 직접 주입/호출 금지.

**3. 도메인 로직 위치**

규칙/불변식은 Entity에 우선 배치.
Entity가 담기 어려운 흐름 조합은 Service에서 처리.

**4. Facade 사용 기준**

여러 Service를 조합하는 복잡한 흐름에만 사용.
단일 Service 호출 수준이면 Facade 불필요.
외부 API 호출(결제, OAuth 등) 포함 시 허용.
