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
| `CLAUDE.md` | Claude AI 작업 지침 (Discord webhook 등) |
| `discord_send.ps1` | Discord 알림 전송 PowerShell 스크립트 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `Mavis-Api/` | 사용자용 REST API 서버 (see `Mavis-Api/AGENTS.md`) |
| `Mavis-Admin/` | 어드민 대시보드 API 서버 (see `Mavis-Admin/AGENTS.md`) |
| `Mavis-Common/` | 공통 DTO, 유틸리티, 예외 정의 (see `Mavis-Common/AGENTS.md`) |
| `Mavis-Domain/` | 도메인 엔티티, 리포지토리, 비즈니스 로직 (see `Mavis-Domain/AGENTS.md`) |
| `Mavis-Infrastructure/` | 외부 서비스 연동 (OAuth, 결제, S3, 이메일, Discord) (see `Mavis-Infrastructure/AGENTS.md`) |
| `Mavis-Submodule/` | 민감 설정 파일 관리용 Git 서브모듈 |
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
- 환경별 설정은 Mavis-Submodule을 통해 관리되므로 application.yml을 직접 수정하지 말 것

### Testing Requirements
- 각 모듈 단위: `./gradlew :<ModuleName>:test`
- 전체 빌드: `./gradlew build`

### Common Patterns
- 헥사고날 아키텍처: 도메인과 인프라를 명확히 분리
- DDD: 비즈니스 도메인별 패키지 구조 (order, product, user, ...)
- QueryDSL을 활용한 커스텀 리포지토리
- Feign Client로 외부 API 호출 추상화

## Dependencies

### External
- Spring Boot 3.x
- Gradle 8.14.3
- Docker / Docker Compose

<!-- MANUAL: -->
