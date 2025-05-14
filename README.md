## API 버전 관리 시스템

이 프로젝트는 Spring MVC에서 효율적인 API 버전 관리를 위한 표준 확장 방식을 구현했습니다.

[API_VERSIONING.md](./docs/API_VERSIONING.md) 참고

## 코드 작성시 유의 사항

API 엔드포인트 작성시 엔드포인트 맨 앞에 `/api`를 반드시 포함해야 합니다.

현재 [WebConfig.java](src/main/java/com/sesac/carematching/config/WebConfig.java)에서 `new ApiVersionRMHM("/api")`를 실행하기 때문에 모든 엔드포인트에 대해 `/api` prefix가 필요합니다.

컨트롤러 코드를 보고 API 엔드포인트를 바로 이해할 수 있도록 코드 가독성을 위해 컨트롤러에 모두 접두사 `/api`를 직접 작성하는 방식으로 작성했습니다.

## Production 환경으로 실행

### Java 명령어를 통해 직접 옵션 주기
`java -Dspring.profiles.active=prod -jar "app.jar"`

### 환경 변수를 통해 옵션 주기
`SPRING_PROFILES_ACTIVE=prod` 를 .env 파일에 추가하거나 환경 변수에 적용해주면 됩니다.
