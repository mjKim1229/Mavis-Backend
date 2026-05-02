<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-18 | Updated: 2026-04-18 -->

# Mavis-Common

## Purpose
전체 모듈에서 공유하는 공통 코드. DTO, 예외 계층, JWT 유틸리티, 상수, 외부 서비스 설정 프로퍼티 등을 제공한다.
이 모듈을 수정하면 모든 모듈에 영향을 준다.

## Key Files

| File | Description |
|------|-------------|
| `src/main/resources/application-common.yml` | 공통 설정 (JWT, Kakao, Naver, TossPayments) |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `annotation/` | `@ExcelColumn` - 엑셀 내보내기용 커스텀 어노테이션 |
| `config/` | 설정 프로퍼티 바인딩 |
| `consts/` | `MavisStatic` - 전역 정적 상수 |
| `dto/` | `ErrorResponse`, `SuccessResponse`, `JwtPair`, `ErrorReason` |
| `enums/` | 상품 카테고리, 공통 Enum 매퍼 |
| `exception/` | JWT 예외, 전역 에러 코드, 베이스 예외 클래스 |
| `jwt/` | `JwtTokenUtil` - 토큰 생성/검증 |
| `properties/` | Jwt, Kakao, Naver, TossPayments 설정 프로퍼티 |
| `util/` | 날짜 포맷터, 주문번호 생성기, 전화번호 정규화, 인증코드 생성기 |

## For AI Agents

### Working In This Directory
- **이 모듈 변경은 전체 빌드에 영향**을 주므로 반드시 전체 테스트 후 반영
- `ErrorCode` 추가 시 기존 코드 번호와 중복되지 않게 확인
- `SuccessResponse` / `ErrorResponse` 구조 변경은 클라이언트(FE) 협의 필요

### Testing Requirements
- `./gradlew :Mavis-Common:test`
- JWT 테스트: `JwtTokenProviderTest`

### Common Patterns
- 모든 API 성공 응답: `SuccessResponse.of(data)`
- 모든 예외: `MavisException(ErrorCode)` 패턴
- JWT: `JwtTokenUtil.generateToken()` / `validateToken()`

## Dependencies

### Internal
- 다른 모듈에 의존하지 않음 (최하위 공통 모듈)

### External
- Spring Boot
- JJWT (JWT 라이브러리)

<!-- MANUAL: -->
