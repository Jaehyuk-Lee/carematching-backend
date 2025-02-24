## Production 환경으로 실행

### Java 명령어를 통해 직접 옵션 주기
`java -Dspring.profiles.active=prod -jar app.jar"`

### 환경 변수를 통해 옵션 주기
`SPRING_PROFILES_ACTIVE=dev` 를 .env 파일에 추가하거나  
환경 변수에 적용해주면 된다.
