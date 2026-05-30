<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-18 | Updated: 2026-04-18 -->

# Mavis-Api

## Purpose
사용자 대상 REST API 서버. 회원 인증, 장바구니, 상품 조회, 주문/결제, 리뷰, 환불, 문의 등
이커머스 핵심 기능을 제공한다. JWT 기반 인증과 Spring Security를 사용한다.

## Key Files

| File | Description |
|------|-------------|
| `build.gradle` | 모듈 의존성 선언 |
| `Dockerfile` | 프로덕션 Docker 이미지 빌드 |
| `src/main/resources/application.yml` | 서버 포트, 보안 설정 등 |
| `src/main/resources/logback-spring.xml` | 로깅 설정 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `src/main/java/.../api/` | 도메인별 컨트롤러, DTO, 서비스, 파사드 |
| `src/main/java/.../config/` | CORS, Swagger, 스케줄러, 예외핸들러 등 설정 |
| `src/main/java/.../global/security/` | JWT 필터, SecurityConfig, 인증 세부 처리 |
| `src/main/java/.../scheduler/` | 결제 정산, 인증 코드 만료 등 배치 작업 |
| `src/test/` | JwtTokenFilter, OrderService, OrderItemAppender 테스트 |

## Domain Packages

| Package | Description |
|---------|-------------|
| `auth/` | 로그인, 회원가입, 소셜로그인, 프로필 관리 |
| `cart/` | 장바구니 CRUD |
| `product/` | 상품 목록/상세/검색 |
| `order/` | 주문 생성, 결제 연동(TossPayments), 주문 조회 |
| `review/` | 리뷰 작성/수정/삭제, 이미지 업로드 |
| `refund/` | 환불 요청 처리 |
| `inquiry/` | 상품 문의 등록/조회 |
| `favorite/` | 즐겨찾기 관리 |
| `notice/` | 공지사항 조회 |
| `common/` | 페이지네이션, 슬라이싱, JPA 공통 베이스 |

## For AI Agents

### Working In This Directory
- 새 API 엔드포인트 추가 시 Controller → DTO → Service/Facade 순서로 작성
- 응답은 `SuccessResponse<T>` 래퍼를 사용
- 예외는 도메인 전용 ErrorCode enum → `MavisException` 패턴 사용
- **새 엔드포인트 추가 시 반드시 `SecurityConfig` 확인** — 공개(`permitAll` 또는 `webSecurityCustomizer`) / 인증 필요(`hasRole("USER")`) 여부를 명시적으로 등록

### Testing Requirements
- `./gradlew :Mavis-Api:test`
- JWT 필터 테스트: `JwtTokenFilterTest`
- 주문 서비스 테스트: `OrderServiceTest`, `OrderItemAppenderTest`
- MockMvc 호출 후 DB 상태 검증 시 `em.flush(); em.clear()` 필요 — 같은 트랜잭션 내 1차 캐시로 인해 실제 DB 반영이 안 보일 수 있음
- S3, 외부 HTTP 클라이언트(Feign 등) 포함 API 통합테스트 시 `@MockitoBean`으로 해당 빈 교체 — 실제 외부 호출 방지 (예: `@MockitoBean S3FileUploader`). `@MockBean`은 deprecated

### Common Patterns
- Facade 패턴: 여러 서비스를 조합하는 복잡한 흐름 (예: `OrderFacade`)
- Reader/Appender 분리: 조회는 `*Reader`, 저장은 `*Appender`로 분리
- DTO 명명: `*Request`, `*Response` suffix 사용

## Dependencies

### Internal
- `Mavis-Common` - JWT 유틸, 공통 DTO, 예외
- `Mavis-Domain` - 엔티티, 리포지토리
- `Mavis-Infrastructure` - OAuth, 결제, S3, 이메일

### External
- Spring Boot Web, Security
- Spring Data JPA
- QueryDSL

<!-- MANUAL: -->
