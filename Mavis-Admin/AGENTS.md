<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-18 | Updated: 2026-04-18 -->

# Mavis-Admin

## Purpose
관리자용 REST API 서버. 상품 등록/수정, 주문 관리, 배송 처리, 환불 승인, 문의 답변, 공지사항 관리 등
운영 업무 전반을 처리한다. 별도 JWT 인증 체계를 사용한다.

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
| `src/main/java/.../admin/` | 도메인별 컨트롤러, DTO, 서비스 |
| `src/main/java/.../global/` | Security 설정, 예외 핸들러 |
| `src/main/java/.../common/` | 로깅, 페이지네이션 유틸리티 |
| `src/test/` | Admin JWT 필터 테스트 |

## Domain Packages

| Package | Description |
|---------|-------------|
| `auth/` | 어드민 로그인, JWT 발급 |
| `product/` | 상품 등록/수정/삭제, 옵션 관리 |
| `order/` | 주문 목록 조회, 상태 변경 |
| `delivery/` | 배송 등록, 상태 업데이트 |
| `refund/` | 환불 요청 승인/거절 |
| `inquiry/` | 문의 답변 등록/수정 |
| `notice/` | 공지사항 CRUD |
| `admin/` | 어드민 계정 조회 구현체 |

## For AI Agents

### Working In This Directory
- 어드민 전용 엔드포인트이므로 반드시 인증 필터 적용 여부 확인
- 상품 이미지 업로드는 `Mavis-Infrastructure`의 S3FileUploader 사용
- 환불 처리는 TossPayments 취소 API 연동 필요 (AdminRefundFacade 참고)
- **새 엔드포인트 추가 시 반드시 `SecurityConfig` 확인** — 공개(`webSecurityCustomizer`) / 인증 필요(`hasRole("ADMIN")`) 여부를 명시적으로 등록

### Testing Requirements
- `./gradlew :Mavis-Admin:test`
- Admin JWT 필터 테스트: `AdminJwtTokenFilterTest`
- MockMvc 호출 후 DB 상태 검증 시 `em.flush(); em.clear()` 필요 — 같은 트랜잭션 내 1차 캐시로 인해 실제 DB 반영이 안 보일 수 있음

### Naming Convention
- 이 모듈의 모든 클래스는 `Admin` prefix 사용 (예: `AdminInquiryService`, `AdminInquiryAnswerService`, `AdminOrderController`)
- Mavis-Api 모듈의 동명 클래스와 충돌 방지 및 어드민 전용임을 명시

### Common Patterns
- Mavis-Api와 유사한 Controller → Service 구조
- Implementer 패턴: 복잡한 저장 로직을 `*Implementer`로 분리

## Dependencies

### Internal
- `Mavis-Common` - JWT 유틸, 공통 DTO
- `Mavis-Domain` - 엔티티, 리포지토리
- `Mavis-Infrastructure` - S3, TossPayments, Discord

### External
- Spring Boot Web, Security
- Spring Data JPA

<!-- MANUAL: -->
