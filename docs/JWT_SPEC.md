# JWT Specification (MSA 공통 스펙)

## 1. 목적
모든 마이크로서비스가 동일한 형태와 검증 방식을 사용하도록 JWT 토큰 구조와 검증 규칙을 표준화합니다.

## 2. 전체 구조
구조: Header.Payload.Signature

- Payload(Claims) 예
```json
{
  "role": "ROLE_USER",
  "userId": 2,
  "username": "user01",
  "sub": "2",
  "iat": 1762161872,
  "exp": 1762171872
}
```

## 3. 필드(Claims) 정의
필수(모든 서비스가 반드시 검증)
- sub (string)
  - 설명: 토큰의 주체(subject). 서비스 전반에서 사용자를 고유 식별하는 값.
  - 사용법: userId 기반 문자열로 설정 (예: userId가 2이면 "2").

- userId (number)
  - 설명: 실제 사용자 ID (auto_increment Integer).
  - 사용법: 권한 체크, DB 연동, 이벤트 생성 시 대표 식별자.

- iat (number)
  - 설명: 토큰 발급 시각 (Issued At) — epoch seconds

- exp (number)
  - 설명: 토큰 만료시간 (Expiration) — epoch seconds

선택(권장 포함)
- username (string): 표시용 사용자명
- role (string) or roles (array): 권한 정보 (예: `ROLE_USER`)

## 4. 발급 규칙
- 발급 시 최소 포함: sub, userId, iat, exp (username/role 권장)

## 5. 서비스 측 검증 절차 (모든 서비스가 구현)
1. 토큰 추출
   - HTTP: Authorization: Bearer <token>
   - WebSocket: handshake.auth.token 또는 query.token
2. 서명 검증
3. 필수 claims 존재 및 타입 확인(sub, userId, iat, exp)
4. 만료(exp) 검증
5. 실패 처리 표준화

권장 에러 코드/메시지 예
- invalid_signature → 401
- token_expired → 401

## 6. WebSocket 적용 시
- 핸드셰이크 단계에서 토큰 검증 후 socket에 userId/username 부착
- 메시지 송신 시 서버는 socket에 부착된 userId를 신뢰
