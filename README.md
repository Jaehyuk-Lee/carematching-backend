# CareMatching: 요양사-환자 매칭 플랫폼

요양사와 환자를 연결하는 매칭 플랫폼 서비스입니다.

## 📘 프로젝트 소개

Care Matching은 요양사와 환자를 효율적으로 연결하는 매칭 플랫폼입니다. 본 서비스는 다음과 같은 주요 기능을 제공합니다:

- 사용자 관리 (회원가입, 로그인, 프로필 관리)
- 요양사 자격 인증 시스템
- 실시간 채팅 기능
- 커뮤니티 게시판 (일반/요양사 전용)
- 결제 시스템

### 🕑 기간

2025.02.10 ~ 2025.03.09 (4주)

### 배경 및 기대효과

* **배경**: 
고령 인구의 지속적인 증가로 요양사에 대한 수요가 빠르게 확대
* **기대효과**: 
검색부터 채팅, 결제까지 한 번에 처리할 수 있는 원스톱 서비스를 제공함으로써, 더욱 편리하고 매끄러운 사용자 경험을 실현

## 🛠️ 기술 스택

### 백엔드

![Java](https://img.shields.io/badge/Java-21-blue?style=flat&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?style=flat-square&logo=Spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat&logo=spring&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=flat&logo=mariadb)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=flat&logo=JSON%20web%20tokens)
![WebSocket](https://img.shields.io/badge/WebSocket-000000?style=flat)


### 프론트엔드
![React](https://img.shields.io/badge/React-61DAFB?style=flat&logo=react&logoColor=black) - [프론트엔드 프로젝트 링크](https://github.com/Jaehyuk-Lee/carematching-front)

## 🗂️ 프로젝트 구조
```
├── src/main/java/com/sesac/carematching
│   ├── caregiver/      # 요양사
│   ├── chat/           # 채팅
│   ├── community/      # 커뮤니티
│   ├── monitoring/     # AWS Health Check용 컨트롤러
│   ├── token/          # JWT Refresh 토큰
│   ├── transaction/    # 결제
│   ├── user/           # 사용자(회원가입, 마이페이지 등)
│   ├── config/         # Spring 설정 및 다양한 핸들러
│   ├── util/           # 유틸
│   └── ...
├── src/main/resources  # Spring 설정, 데이터베이스 초기값
└── ...
```

## 📄 API 문서

API 문서는 Swagger UI를 통해 제공됩니다:
- 개발 환경: http://localhost:8080/swagger-ui.html

## ✨ 주요 기능

### 1. 사용자 관리
- 회원가입/로그인 (JWT 기반 인증)
- 사용자 프로필 관리
- 요양사 자격 인증 시스템
- 권한 관리 (일반 사용자, 요양사, 관리자)

### 2. 실시간 채팅
- WebSocket을 활용한 실시간 메시지 전송
- 채팅방 생성
- 메시지 히스토리 저장

### 3. 커뮤니티
- 일반 게시판
- 요양사 전용 게시판
- 게시글 CRUD
- 댓글 및 좋아요 기능
- 조회수 관리

### 4. 결제 시스템
- 토스 페이먼츠 기반 결제

## 🗂️ API 버전 관리

이 프로젝트는 효율적인 API 버전 관리를 위한 URL 기반 버전 시스템을 구현했습니다.

[API_VERSIONING.md](./docs/API_VERSIONING.md) 참고

## 📝 코드 작성시 유의사항

API 엔드포인트 작성시 엔드포인트 맨 앞에 `/api`를 반드시 포함해야 합니다.

현재 [WebConfig.java](./src/main/java/com/sesac/carematching/config/WebConfig.java)에서 `new ApiVersionRMHM("/api")`를 실행하기 때문에 모든 엔드포인트에 대해 `/api` prefix가 필요합니다.

컨트롤러 코드를 보고 API 엔드포인트를 바로 이해할 수 있도록 코드 가독성을 위해 컨트롤러에 모두 접두사 `/api`를 직접 작성하는 방식으로 작성했습니다.

## ⚙️ 빌드 및 실행 방법

1. **프로젝트 클론**
    ```bash
    git clone https://github.com/Jaehyuk-Lee/carematching-backend.git
    ```

2. **외부 데이터베이스 준비**
    - MariaDB, Redis 필요

3. **환경 변수 설정**
    - [.env.template](./.env.template) 파일을 복사해 `.env`로 이름 변경 후 모든 항목 채우기
    - `DB_` : MariaDB 설정
    - `JWT_` : JWT 설정
    - `S3_` : AWS S3 설정
    - `REDIS_` : Redis 설정

4. **프로젝트 빌드 및 실행**
    ```bash
    ./gradlew build
    java -jar build/libs/carematching-*-SNAPSHOT.jar
    ```

5. **Production 환경 실행**
    - Java 옵션:  
      ```bash
      java -Dspring.profiles.active=prod -jar "build/libs/carematching-*-SNAPSHOT.jar"
      ```
    - 또는 환경변수에 `SPRING_PROFILES_ACTIVE=prod` 추가
