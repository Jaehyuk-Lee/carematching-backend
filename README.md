## API 버전 관리 시스템

이 프로젝트는 Spring MVC에서 효율적인 API 버전 관리를 위한 표준 확장 방식을 구현했습니다.

[API_VERSIONING.md](./docs/API_VERSIONING.md) 참고

## Production 환경으로 실행

### Java 명령어를 통해 직접 옵션 주기
`java -Dspring.profiles.active=prod -jar "app.jar"`

### 환경 변수를 통해 옵션 주기
`SPRING_PROFILES_ACTIVE=prod` 를 .env 파일에 추가하거나  
환경 변수에 적용해주면 된다.
