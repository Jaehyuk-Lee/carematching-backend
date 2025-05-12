# CareMatching

요양사와 환자를 연결하는 매칭 플랫폼 서비스입니다.

## 프로젝트 소개

Care Matching은 요양사와 환자를 효율적으로 연결하는 매칭 플랫폼입니다. 본 서비스는 다음과 같은 주요 기능을 제공합니다:

- 사용자 관리 (회원가입, 로그인, 프로필 관리)
- 요양사 자격 인증 시스템
- 실시간 채팅 기능
- 커뮤니티 게시판 (일반/요양사 전용)
- 매칭 시스템

## 기술 스택

### 백엔드
- Java 21
- Spring Boot 3.x
- Spring Security + JWT
- Spring Data JPA
- MariaDB
- MongoDB (추가 중)
- Redis
- WebSocket

### 프론트엔드
- React - [프론트엔드 프로젝트 링크](https://github.com/Jaehyuk-Lee/carematching-front)

## 주요 기능

### 1. 사용자 관리
- 회원가입/로그인 (JWT 기반 인증)
- 사용자 프로필 관리
- 요양사 자격 인증 시스템
- 권한 관리 (일반 사용자, 요양사, 관리자)

### 2. 실시간 채팅
- WebSocket을 활용한 실시간 메시지 전송
- 채팅방 생성 및 관리
- 메시지 히스토리 저장

### 3. 커뮤니티
- 일반 게시판
- 요양사 전용 게시판
- 게시글 CRUD
- 댓글 및 좋아요 기능
- 조회수 관리

### 4. 매칭 시스템
- 요양사와 환자 간 매칭
- 매칭 상태 관리
- 리뷰 및 평가 시스템

## 실행 방법

1. 프로젝트 클론
```bash
git clone https://github.com/Jaehyuk-Lee/carematching-backend.git
```

2. 환경 변수 설정

[.env.template](./.env.template) 파일을 복사해서 `.env`로 이름을 바꾸고, 모든 항목을 채워주세요.

3. 프로젝트 빌드 및 실행
```bash
./gradlew build
java -jar build/libs/carematching-0.0.1-SNAPSHOT.jar
```

## API 문서

API 문서는 Swagger UI를 통해 제공됩니다:
- 개발 환경: http://localhost:8080/swagger-ui.html

## API 버전 관리 시스템

이 프로젝트는 Spring MVC에서 효율적인 API 버전 관리를 위한 표준 확장 방식을 구현했습니다.

[API_VERSIONING.md](./docs/API_VERSIONING.md) 참고

## 코드 작성시 유의 사항

API 엔드포인트 작성시 엔드포인트 맨 앞에 `/api`를 반드시 포함해야 합니다.

현재 [WebConfig.java](../src/main/java/com/sesac/carematching/config/WebConfig.java)에서 `new ApiVersionRMHM("/api")`를 실행하기 때문에 모든 엔드포인트에 대해 `/api` prefix가 필요합니다.

컨트롤러 코드를 보고 API 엔드포인트를 바로 이해할 수 있도록 코드 가독성을 위해 컨트롤러에 모두 접두사 `/api`를 직접 작성하는 방식으로 작성했습니다.

## Production 환경으로 실행

### Java 명령어를 통해 직접 옵션 주기
`java -Dspring.profiles.active=prod -jar "app.jar"`

### 환경 변수를 통해 옵션 주기
`SPRING_PROFILES_ACTIVE=prod` 를 .env 파일에 추가하거나 환경 변수에 적용해주면 됩니다.
