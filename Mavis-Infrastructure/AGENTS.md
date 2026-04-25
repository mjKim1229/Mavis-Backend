<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-04-18 | Updated: 2026-04-18 -->

# Mavis-Infrastructure

## Purpose
외부 서비스 연동을 담당하는 인프라 레이어. OAuth 소셜 로그인, TossPayments 결제 게이트웨이,
AWS S3 파일 업로드, 이메일 발송, Discord 웹훅 알림을 Feign Client로 추상화하여 제공한다.

## Key Files

| File | Description |
|------|-------------|
| `src/main/resources/application-infrastructure.yml` | 외부 서비스 URL, 키 설정 |
| `src/main/resources/templates/mail/reset-password.html` | 비밀번호 재설정 이메일 템플릿 |
| `src/main/resources/templates/mail/verify.html` | 이메일 인증 템플릿 |
| `src/main/resources/templates/mail/welcome.html` | 가입 환영 이메일 템플릿 |

## Subdirectories

| Directory | Purpose |
|-----------|---------|
| `oauth/` | Kakao / Naver 소셜 로그인 Feign 클라이언트 |
| `tosspayments/` | TossPayments 결제 승인/취소/조회 Feign 클라이언트 |
| `discord/` | Discord 웹훅 알림 서비스 |
| `email/` | Spring Mail 이메일 발송 서비스 |
| `image/` | AWS S3 파일 업로드 (`S3FileUploader`) |

## Package Details

### oauth/
| File | Description |
|------|-------------|
| `KakaoOAuthClient` | Kakao 액세스 토큰 발급 Feign 클라이언트 |
| `KakaoInfoClient` | Kakao 사용자 정보 조회 Feign 클라이언트 |
| `NaverOAuthClient` | Naver 액세스 토큰 발급 Feign 클라이언트 |
| `NaverInfoClient` | Naver 사용자 정보 조회 Feign 클라이언트 |

### tosspayments/
| File | Description |
|------|-------------|
| `PaymentsConfirmClient` | 결제 승인 API |
| `PaymentsCancelClient` | 결제 취소 API |
| `PaymentsQueryClient` | 결제 내역 조회 API |
| `TossPaymentsConfig` | 에러 디코더 포함 Feign 설정 |

### discord/
| File | Description |
|------|-------------|
| `DiscordWebhookClient` | Discord 웹훅 Feign 클라이언트 |
| `DiscordNotificationService` | 알림 메시지 조합 및 발송 서비스 |

## For AI Agents

### Working In This Directory
- Feign Client URL과 인증 키는 `application-infrastructure.yml` 또는 `Mavis-Submodule` 설정 파일에서 주입됨
- TossPayments 에러 처리는 `TossPaymentsErrorDecoder`를 통해 표준 예외로 변환
- S3 업로드 시 파일 크기/타입 검증은 호출 측(API 레이어)에서 수행
- 이메일 템플릿 수정 시 HTML 파일 직접 편집 (Thymeleaf 템플릿)

### Testing Requirements
- `./gradlew :Mavis-Infrastructure:test`
- 외부 API 연동 테스트는 모킹 필수 (실제 API 호출 금지)

### Common Patterns
- 모든 외부 HTTP 호출: `OpenFeign` 인터페이스 선언
- 설정 분리: 각 외부 서비스별 `*Config` 클래스
- 예외 변환: 외부 API 오류 → 내부 `MavisException`으로 변환

## Dependencies

### Internal
- `Mavis-Common` - 공통 예외, 프로퍼티

### External
- Spring Cloud OpenFeign
- AWS SDK (S3)
- Spring Mail (JavaMailSender)
- Thymeleaf (이메일 템플릿)

<!-- MANUAL: -->
