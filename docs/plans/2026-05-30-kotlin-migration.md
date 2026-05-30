# Kotlin 점진적 전환 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 멀티모듈 Spring Boot(Java 21) 프로젝트를 빌드를 깨뜨리지 않고 Java/Kotlin 공존 상태로 만들어 모듈·패키지 단위로 Kotlin으로 점진 전환한다. 첫 파일럿은 `:infra:gateway`.

**Architecture:** Kotlin Gradle 플러그인을 모듈별로 추가해 같은 모듈에서 Java와 Kotlin이 함께 컴파일되도록 한다(코틀린 → JVM 바이트코드라 Java↔Kotlin 상호 호출 가능). Spring/JPA 관용을 위해 `kotlin-spring`(allopen), `kotlin-jpa`(noarg) 플러그인을 적용하고, 전환된 클래스는 Lombok을 제거하고 data class/프로퍼티/Kotlin 관용구로 바꾼다. 회귀는 각 모듈의 기존 테스트와 부팅 검증으로 보장한다.

**Tech Stack:** Spring Boot 3.4.2, Java 21 toolchain, Gradle 8.12.1, Kotlin 1.9.25(Spring Boot 3.4 관리 버전과 정렬), `org.jetbrains.kotlin.plugin.spring` / `.jpa`, JUnit5 + WireMock.

---

## 전환 원칙 (전 작업 공통)

- **공존 우선:** 한 번에 한 패키지/계층씩. 매 태스크 후 `./gradlew :<module>:build` 가 통과해야 한다.
- **Lombok 제거는 전환되는 클래스에 한정:** Java로 남는 클래스의 Lombok은 그대로 둔다. 한 클래스를 Kotlin으로 옮길 때만 그 클래스의 Lombok 어노테이션을 제거한다.
- **Java 호출자 호환:** Kotlin `val name`/`var name` 은 Java에서 `getName()`/`setName()` 으로 보이므로 getter/setter 호출자는 그대로 동작한다. 단 Lombok `@Builder`로 만든 빌더는 사라지므로, 빌더를 쓰던 Java 호출부는 named-argument 생성자 호출로 바꾸거나 전환 대상에 포함해야 한다(→ "리프부터" 순서의 이유).
- **leaf-first 순서:** 의존을 적게 받는 잎(엔티티/DTO/enum/유틸) → repository → service → controller/config 순으로 올린다.
- **커밋 단위:** 파일 1개(또는 밀접한 소수) 전환 = 커밋 1개. 빌드 그린 상태에서만 커밋.
- **자동 변환 보조:** IntelliJ "Convert Java File to Kotlin"을 초안으로 쓰되, 결과를 반드시 손으로 정리(nullable 남발/`!!`/플랫폼 타입 정리). CLI 세션에서는 수동 작성.

---

## Phase 0 — 파일럿: `:infra:gateway` 모듈 전환

> 대상 파일: `GatewayApplication.java`, `config/Http2UpstreamConfig.java`, 안전망 테스트 `GatewayRoutingTests.java`. `application.yaml`은 변경 없음.

### Task 0.1: 기준 그린 확인 (전환 전 baseline)

**Files:** 없음(검증만)

**Step 1: 기존 테스트가 통과하는지 확인**

Run: `./gradlew :infra:gateway:test`
Expected: PASS (`GatewayRoutingTests > gateway_should_route_to_platform()` 통과). 이 테스트가 Phase 0 전체의 회귀 안전망이다.

**Step 2: baseline 커밋(선택)**

작업 트리가 깨끗하지 않다면 현재 상태를 먼저 정리/커밋한다.

---

### Task 0.2: gateway 빌드에 Kotlin 플러그인 추가 (공존 활성화)

**Files:**
- Modify: `infra/gateway/build.gradle`

**Step 1: 빌드 스크립트 수정**

`plugins { ... }` 블록에 Kotlin 플러그인 2종을 추가하고, Kotlin 컴파일 타깃을 JVM 21로 맞추고, stdlib 의존성을 추가한다. (gateway는 JPA가 없으므로 `kotlin.plugin.jpa`는 불필요.)

```groovy
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.9.25'
    id 'org.jetbrains.kotlin.plugin.spring' version '1.9.25'
    id 'org.springframework.boot' version '3.4.2'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.sesac.carematching'
version = '0.0.1-SNAPSHOT'

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:2024.0.2"
    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway'

    // Kotlin
    implementation 'org.jetbrains.kotlin:kotlin-reflect'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.wiremock:wiremock-standalone:3.13.1'
}

tasks.named('test') {
    useJUnitPlatform()
}

tasks.named('jar') {
    enabled = false
}
```

> 참고: `kotlin-stdlib`/`kotlin-reflect` 버전은 Spring Boot 3.4.2 BOM이 1.9.25로 관리하므로 버전 표기 없이 추가한다(플러그인 1.9.25와 정렬).

**Step 2: Kotlin 소스셋이 인식되도록 디렉터리 생성**

Run: `mkdir -p infra/gateway/src/main/kotlin infra/gateway/src/test/kotlin`

**Step 3: 빌드가 여전히 통과하는지 확인 (코드 변환 전, 플러그인만 추가)**

Run: `./gradlew :infra:gateway:build`
Expected: PASS. Java 소스만 있는 상태에서 Kotlin 플러그인을 얹어도 빌드가 깨지지 않음을 확인.

**Step 4: 커밋**

```bash
git add infra/gateway/build.gradle
git commit -m "build(gateway): Kotlin/Spring 플러그인 추가 (Java/Kotlin 공존 활성화)"
```

---

### Task 0.3: `Http2UpstreamConfig` 를 Kotlin으로 전환

**Files:**
- Create: `infra/gateway/src/main/kotlin/com/sesac/carematching/infra/gateway/config/Http2UpstreamConfig.kt`
- Delete: `infra/gateway/src/main/java/com/sesac/carematching/infra/gateway/config/Http2UpstreamConfig.java`

**Step 1: Kotlin 버전 작성**

```kotlin
package com.sesac.carematching.infra.gateway.config

import org.springframework.cloud.gateway.config.HttpClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.netty.http.HttpProtocol

@Configuration
class Http2UpstreamConfig {

    @Bean
    fun http2ClientCustomizer(): HttpClientCustomizer =
        HttpClientCustomizer { httpClient -> httpClient.protocol(HttpProtocol.H2C) }
}
```

> `kotlin-spring`(allopen) 플러그인이 `@Configuration` 클래스를 자동으로 open 처리하므로 `open` 키워드는 불필요.

**Step 2: 기존 Java 파일 삭제**

Run: `rm infra/gateway/src/main/java/com/sesac/carematching/infra/gateway/config/Http2UpstreamConfig.java`

**Step 3: 회귀 테스트로 검증**

Run: `./gradlew :infra:gateway:test`
Expected: PASS — `gateway_should_route_to_platform()` 가 여전히 통과(라우팅·HttpClient 설정 정상).

**Step 4: 커밋**

```bash
git add infra/gateway/src/main/kotlin/com/sesac/carematching/infra/gateway/config/Http2UpstreamConfig.kt
git add -u infra/gateway/src/main/java/com/sesac/carematching/infra/gateway/config/Http2UpstreamConfig.java
git commit -m "refactor(gateway): Http2UpstreamConfig Kotlin 전환"
```

---

### Task 0.4: `GatewayApplication` 을 Kotlin으로 전환

**Files:**
- Create: `infra/gateway/src/main/kotlin/com/sesac/carematching/infra/gateway/GatewayApplication.kt`
- Delete: `infra/gateway/src/main/java/com/sesac/carematching/infra/gateway/GatewayApplication.java`

**Step 1: Kotlin 버전 작성**

```kotlin
package com.sesac.carematching.infra.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GatewayApplication

fun main(args: Array<String>) {
    runApplication<GatewayApplication>(*args)
}
```

> 테스트가 `classes = GatewayApplication.class` 를 참조하는데, Kotlin `class GatewayApplication` 도 동일한 FQCN(`...GatewayApplication`)으로 컴파일되므로 테스트 수정 불필요. (top-level `main`은 별도 `GatewayApplicationKt` 클래스로 생성되며 부팅에는 영향 없음.)

**Step 2: 기존 Java 파일 삭제**

Run: `rm infra/gateway/src/main/java/com/sesac/carematching/infra/gateway/GatewayApplication.java`

**Step 3: 부팅·라우팅 회귀 검증**

Run: `./gradlew :infra:gateway:test`
Expected: PASS — `@SpringBootTest(classes = GatewayApplication.class)` 컨텍스트가 정상 기동하고 라우팅 테스트 통과.

**Step 4: 빈 Java 디렉터리 정리 + 커밋**

```bash
find infra/gateway/src/main/java -type d -empty -delete
git add infra/gateway/src/main/kotlin/com/sesac/carematching/infra/gateway/GatewayApplication.kt
git add -u
git commit -m "refactor(gateway): GatewayApplication Kotlin 전환"
```

---

### Task 0.5: 안전망 테스트(`GatewayRoutingTests`) Kotlin 전환

> 프로덕션 코드를 다 옮긴 뒤 마지막에 테스트를 옮긴다(테스트가 그린인 상태에서 프로덕션을 전환했고, 이제 테스트 자체를 전환).

**Files:**
- Create: `infra/gateway/src/test/kotlin/com/sesac/carematching/infra/gateway/GatewayRoutingTests.kt`
- Delete: `infra/gateway/src/test/java/com/sesac/carematching/infra/gateway/GatewayRoutingTests.java`

**Step 1: Kotlin 버전 작성**

```kotlin
package com.sesac.carematching.infra.gateway

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(classes = [GatewayApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayRoutingTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun gateway_should_route_to_platform() {
        wireMockServer.stubFor(
            get(urlEqualTo("/hello"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("hello from platform"),
                ),
        )

        webTestClient.get().uri("/platform/hello")
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java).isEqualTo("hello from platform")

        wireMockServer.verify(getRequestedFor(urlEqualTo("/hello")))
    }

    companion object {
        private lateinit var wireMockServer: WireMockServer

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            wireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
            wireMockServer.start()
            // application.yaml에서 사용하는 프로퍼티에 WireMock 포트를 주입
            registry.add("carematching.platform.uri") { "localhost:" + wireMockServer.port() }
        }

        @JvmStatic
        @AfterAll
        fun stopWireMock() {
            wireMockServer.stop()
        }
    }
}
```

> 정적 멤버(`@DynamicPropertySource`, `@AfterAll`)는 Kotlin `companion object` + `@JvmStatic` 으로 옮긴다.

**Step 2: 기존 Java 테스트 삭제**

Run: `rm infra/gateway/src/test/java/com/sesac/carematching/infra/gateway/GatewayRoutingTests.java`

**Step 3: 전체 검증**

Run: `./gradlew :infra:gateway:build`
Expected: PASS — 컴파일 + 테스트 그린. 이 시점에서 gateway 모듈은 100% Kotlin.

**Step 4: 빈 디렉터리 정리 + 커밋**

```bash
find infra/gateway/src/test/java -type d -empty -delete
git add infra/gateway/src/test/kotlin/com/sesac/carematching/infra/gateway/GatewayRoutingTests.kt
git add -u
git commit -m "test(gateway): GatewayRoutingTests Kotlin 전환 — gateway 모듈 Kotlin 전환 완료"
```

---

### Task 0.6: 파일럿 회고 체크포인트

**Files:** 없음

**Step 1: 파일럿에서 확인할 것 정리**

- [ ] Java/Kotlin 공존 빌드가 문제없이 동작했는가
- [ ] allopen(`kotlin-spring`)으로 `@Configuration`/`@SpringBootTest`가 정상 동작했는가
- [ ] 부팅·라우팅 회귀 테스트가 끝까지 그린이었는가
- [ ] Kotlin 버전(1.9.25)과 Spring Boot BOM 관리 버전 사이 경고/충돌이 없었는가

**Step 2: 사용자에게 Phase 1(app 모듈) 진행 여부 확인**

파일럿 결과를 사용자에게 보고하고, app 모듈 전환을 이어갈지 결정을 받는다.

---

## Phase 1 — `:app` 모듈 Kotlin 툴체인 활성화 (코드 변환 없음)

> 169개 Java 파일 + Lombok + JPA. 먼저 **공존 가능한 빌드**만 만든다. 이 Phase에서는 Java 코드를 전혀 건드리지 않으며, 빌드가 기존과 동일하게 통과하는 것이 성공 기준.

### Task 1.1: app baseline 그린 확인

**Step 1:** Run: `./gradlew :app:build`
Expected: PASS (또는 현재 알려진 상태 그대로). 변환 전 기준선을 기록한다.

---

### Task 1.2: app `build.gradle` 에 Kotlin + JPA/Spring 플러그인과 kapt 추가

**Files:**
- Modify: `app/build.gradle`

**Step 1: plugins 블록 수정**

```groovy
plugins {
	id 'java'
	id 'org.jetbrains.kotlin.jvm' version '1.9.25'
	id 'org.jetbrains.kotlin.plugin.spring' version '1.9.25'   // allopen: @Component/@Service/@Configuration/@Transactional 등
	id 'org.jetbrains.kotlin.plugin.jpa' version '1.9.25'      // noarg: @Entity/@Embeddable/@MappedSuperclass 기본 생성자
	id 'org.jetbrains.kotlin.kapt' version '1.9.25'            // 전환 과도기 동안 Kotlin 쪽 Lombok/어노테이션 프로세서
	id 'org.springframework.boot' version '3.4.2'
	id 'io.spring.dependency-management' version '1.1.7'
}
```

**Step 2: Kotlin 컴파일 옵션 + stdlib 추가**

`repositories { }` 아래에 추가:

```groovy
kotlin {
	compilerOptions {
		jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
		freeCompilerArgs.add('-Xjsr305=strict') // Spring nullability 어노테이션을 엄격 해석
	}
}
```

`dependencies { }` 에 추가:

```groovy
	// Kotlin
	implementation 'org.jetbrains.kotlin:kotlin-reflect'
```

**Step 3: 과도기 Lombok–Kotlin 상호운용 결정**

기본 원칙은 "Kotlin으로 옮기는 클래스에서는 Lombok 제거"이므로 **Kotlin 코드가 Lombok에 의존하지 않는다.** 따라서 `kapt 'org.projectlombok:lombok'` 은 추가하지 않는다(불필요한 kapt 라운드 회피). Java 쪽 `annotationProcessor 'lombok'` 은 그대로 유지한다.

> kapt 플러그인 자체는 향후 Kotlin 측 다른 어노테이션 프로세서(예: MapStruct 도입 시)를 위해 선언만 해두되, 현재 프로세서 등록은 하지 않는다. 만약 빌드 단순화를 원하면 이 단계에서 kapt 플러그인 라인을 빼도 무방하다.

**Step 4: Kotlin 소스셋 디렉터리 생성**

Run: `mkdir -p app/src/main/kotlin app/src/test/kotlin`

**Step 5: 변환 0건 상태로 빌드 검증**

Run: `./gradlew :app:build`
Expected: PASS — Java/Lombok 코드는 그대로이고 Kotlin 플러그인만 얹힌 상태에서 빌드 그린.

**Step 6: 커밋**

```bash
git add app/build.gradle
git commit -m "build(app): Kotlin/Spring/JPA 플러그인 추가 (Java/Kotlin 공존 활성화)"
```

---

## Phase 2 — `:app` 패키지별 점진 전환

> 169개 파일을 한 문서에 모두 풀어쓰지 않는다. 각 파일 전환은 **동일한 기계적 레시피**를 따르므로, 아래 (A) 전환 레시피와 (B) 권장 순서를 따라 패키지 단위로 반복한다. (C)에 첫 리프 패키지의 bite-sized 예시를 둔다.

### (A) 1파일 전환 레시피 (모든 전환 태스크 공통)

각 Java 파일 `Foo.java` → `Foo.kt` 전환 시:

1. `src/main/kotlin/<같은 패키지 경로>/Foo.kt` 생성, Kotlin으로 작성.
   - Lombok 어노테이션 제거. 매핑:
     - `@Getter/@Setter/@Data` → Kotlin 프로퍼티(`val`/`var`). getter/setter는 자동 생성되어 Java 호출자 호환.
     - `@NoArgsConstructor` (JPA 엔티티) → `kotlin-jpa` noarg 플러그인이 처리(직접 작성 불필요).
     - `@AllArgsConstructor/@RequiredArgsConstructor` → 주 생성자.
     - `@Builder` → named-argument 생성자 호출로 대체(빌더를 쓰던 **호출부**도 함께 수정해야 함 → 그래서 leaf-first).
     - `@Slf4j` → `private val log = LoggerFactory.getLogger(Foo::class.java)` 또는 companion object.
     - `@EqualsAndHashCode/@ToString` → DTO/값 객체면 `data class`. **JPA 엔티티는 `data class` 금지**((D) 참고).
   - 널 가능성: Java 플랫폼 타입을 그대로 두지 말고, 실제 계약에 맞춰 `Type` / `Type?` 명시. 무지성 `!!` 금지.
   - DI: 생성자 주입을 주 생성자로. `@Autowired` 필드 주입은 생성자 주입으로 정리.
2. 기존 `Foo.java` 삭제: `rm src/main/java/<경로>/Foo.java`
3. Run: `./gradlew :app:compileKotlin :app:compileJava`
   Expected: PASS — Java 호출부와 Kotlin이 함께 컴파일. 실패 시 호출부 호환(getter/builder/널) 문제이므로 즉시 수정.
4. Run: `./gradlew :app:build` (테스트 포함)
   Expected: PASS.
5. 빈 디렉터리 정리 후 커밋: `refactor(app): <패키지> <클래스> Kotlin 전환`

> **검증 공백 주의:** app 모듈은 테스트가 사실상 없으므로(2개) 컴파일 통과 = 동작 보장이 아니다. 전환 위험이 높은 도메인(특히 `transaction/payment` 상태기계, 결제 provider)은 (E)의 "전환 전 특성화 테스트(characterization test)"를 먼저 추가하는 것을 강력 권장한다.

### (B) 권장 전환 순서 (leaf-first)

의존을 적게 받는 것부터:

1. **enum / 상수 / 순수 유틸** — `user/role`, `util`, 각 도메인의 enum·상태값. 의존 없음, 위험 최저.
2. **DTO / request·response / 값 객체** — `*/dto`, `chat/dto`, `caregiver/dto`, `user/dto`, `transaction/dto`. `data class` 적용.
3. **JPA 엔티티 / 도큐먼트** — 각 도메인 엔티티, `elasticsearch/document`, MongoDB 도큐먼트. **(D) 엔티티 규칙 준수.**
4. **Repository 인터페이스** — Spring Data 인터페이스는 거의 1:1 변환.
5. **Service / 도메인 로직** — 비즈니스 로직. 위험 높음 → (E) 특성화 테스트 우선.
6. **Controller / config / aop / exception handler / filter** — 진입점·인프라.
7. **`CarematchingApplication`(메인 클래스)** — 마지막. (gateway Task 0.4와 동일 패턴.)
8. 도메인 묶음 권장 진행 순서(독립성 높은 순): `community` → `caregiver` → `chat`/`notification` → `user`/`token`/security → `transaction`(가장 마지막, 가장 위험).

### (C) 첫 리프 패키지 bite-sized 예시 — `user/role`

> 실제 파일 구성은 전환 시점에 `ls app/src/main/java/com/sesac/carematching/user/role` 로 확인하고 아래 패턴을 적용한다. 여기서는 enum 1개를 예로 든다.

#### Task 2.C.1: role enum Kotlin 전환

**Files:**
- Create: `app/src/main/kotlin/com/sesac/carematching/user/role/Role.kt`
- Delete: `app/src/main/java/com/sesac/carematching/user/role/Role.java`

**Step 1: 원본 확인**

Run: `cat app/src/main/java/com/sesac/carematching/user/role/Role.java`
(필드/메서드가 있으면 Kotlin enum의 프로퍼티/함수로 옮긴다.)

**Step 2: Kotlin enum 작성 (단순 enum 예시)**

```kotlin
package com.sesac.carematching.user.role

enum class Role {
    USER,
    CAREGIVER,
    ADMIN,
}
```

**Step 3: 원본 삭제**

Run: `rm app/src/main/java/com/sesac/carematching/user/role/Role.java`

**Step 4: 컴파일 검증**

Run: `./gradlew :app:compileKotlin :app:compileJava`
Expected: PASS — `Role` 을 참조하던 Java 코드(`Role.USER` 등)가 그대로 동작.

**Step 5: 전체 빌드 + 커밋**

```bash
./gradlew :app:build
git add app/src/main/kotlin/com/sesac/carematching/user/role/Role.kt
git add -u
git commit -m "refactor(app): user/role Role enum Kotlin 전환"
```

> 이후 같은 패키지의 나머지 파일, 그리고 (B)의 다음 그룹으로 (A) 레시피를 반복한다. 한 패키지를 끝낼 때마다 `./gradlew :app:build` 그린을 커밋 경계로 삼는다.

### (D) JPA 엔티티 전환 규칙 (중요)

- **`data class` 사용 금지.** `equals/hashCode`가 모든 프로퍼티 기반으로 생성되어 지연 로딩/프록시·연관관계에서 문제가 생긴다. 일반 `class` 로 작성.
- `kotlin-jpa`(noarg) 플러그인이 `@Entity` 기본 생성자를 합성하므로 `@NoArgsConstructor` 불필요.
- `kotlin-spring`(allopen)이 `@Entity`를 open으로 만들지 않는다 → `kotlin-jpa` 플러그인이 엔티티 lazy 프록시를 위해 필요한 처리를 담당. (allopen 대상은 Spring 스테레오타입.)
- 가변 프로퍼티는 `var`, 식별자·불변 필드는 `val`. 연관관계 컬렉션은 `mutableListOf()` 등으로 초기화.
- 양방향 연관관계의 `toString()`/`equals()`는 순환참조 위험 → 직접 구현하거나 id 기반으로 제한.
- 지연 로딩 프록시를 위해 엔티티 클래스/프로퍼티가 final이면 안 되는 경우, `all-open`에 `jakarta.persistence.Entity`를 추가하는 옵션도 검토(`allOpen { annotation 'jakarta.persistence.Entity' }`). 단 `kotlin-jpa` 적용 시 일반적으로 별도 설정 없이 동작.

### (E) 고위험 도메인 특성화 테스트 (transaction/payment)

전환 전, 현재 Java 동작을 고정하는 테스트를 먼저 추가한다(superpowers:test-driven-development 참고는 신규 기능용; 여기선 **characterization test**):

- 대상: `TransactionStatus` 상태기계 전이, `PaymentServiceFactory`/PG 선정, Circuit Breaker fallback 일관성(최근 커밋 `c21528b`, `a0feac8` 관련).
- 절차: (1) Java 상태에서 핵심 시나리오 테스트 작성 → 그린 확인 → 커밋. (2) 그 보호 아래 Kotlin 전환. (3) 동일 테스트 그린 유지.
- 이렇게 하면 컴파일만으로 검증 못 하는 로직 회귀를 잡는다.

---

## 전환 중 흔한 함정 체크리스트

- **Lombok `@Builder` 호출부:** 빌더가 사라지면 컴파일 에러. leaf-first 순서를 지키고, 빌더 호출 Java 코드가 남아 있으면 named-argument 생성자로 바꾼다.
- **플랫폼 타입 NPE:** Java가 반환한 값을 Kotlin이 non-null로 받았다가 런타임 NPE. 경계에서 nullable 명시.
- **`lateinit` 남용:** 필드 주입 대신 생성자 주입을 우선.
- **JPA 엔티티 `data class`:** 금지 ((D)).
- **정적 멤버:** Java `static` → Kotlin `companion object` + 필요 시 `@JvmStatic`(JUnit `@AfterAll`/`@BeforeAll`, `@DynamicPropertySource` 등).
- **`@Value`/`@ConfigurationProperties`:** 생성자 바인딩 시 Kotlin 기본값/`val` 조합 확인.
- **Kotlin–Spring 프록시:** `@Transactional`/`@Async` 메서드는 allopen으로 open 처리되지만, `private` 메서드엔 프록시가 안 걸리는 Java와 동일한 제약 유지.
- **버전 정렬:** Kotlin 플러그인(1.9.25)과 Spring Boot BOM이 관리하는 `kotlin-stdlib` 버전이 어긋나면 경고 → 둘 다 1.9.25로 유지.

## 롤백 전략

- 각 전환은 파일 단위 커밋이므로, 문제가 생기면 해당 커밋만 `git revert` 하면 Java 원본으로 즉시 복귀.
- Phase 1(빌드 플러그인 추가)은 코드 변환 0건이므로, 전환을 중단해도 빌드는 Java로 그대로 동작.

---

## 진행 요약

| Phase | 범위 | 검증 기준 |
|---|---|---|
| 0 | gateway 4파일 전면 Kotlin (파일럿) | `:infra:gateway:build` 그린, WireMock 라우팅 테스트 통과 |
| 1 | app 빌드 Kotlin 활성화 (코드 0건) | `:app:build` 그린(기존과 동일) |
| 2 | app 패키지별 leaf-first 전환 | 패키지마다 `:app:build` 그린, 고위험 도메인은 특성화 테스트 그린 |
