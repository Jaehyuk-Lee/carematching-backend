# API Gateway 라우팅

## 라우팅 경로 정리

| 서비스명         | Gateway Path   | 로컬 테스트 URI       | 프로토콜  | 비고             |
| ---------------- | -------------- | --------------------- | --------- | ---------------- |
| platform-service | /platform/**   | http://localhost:8081 | HTTP      | 플랫폼 API       |
| chat-service     | /chat/**       | http://localhost:8082 | HTTP      | 채팅 REST API    |
| chat-service-ws  | /chat/**       | ws://localhost:8082   | WebSocket | 채팅 실시간 연결 |

## Gateway 라우팅 정책 요약

- 모든 서비스는 /{service-name}/** 형태의 prefix를 사용합니다.
- Gateway는 각 서비스의 prefix를 제거(StripPrefix=1)하고 내부 URI로 전달합니다.
  - 예시: `[gateway 주소]/platform/api/v2/caregivers` → `[platform 주소]/api/v2/caregivers`
- 상세 설정은 [resources/application.yaml](/infra/gateway/src/main/resources/application.yaml) 참고
