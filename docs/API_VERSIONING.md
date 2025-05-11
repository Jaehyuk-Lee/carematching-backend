# API 버전 관리 시스템

이 프로젝트는 Spring MVC에서 효율적인 API 버전 관리를 위한 표준 확장 방식을 구현했습니다.

## 주요 기능

- 단일 컨트롤러로 여러 API 버전 지원
- 버전별 코드 중복 없음
- 새 버전 추가 시 기존 코드 수정 최소화
- 이전 버전과의 호환성 유지
- 버전 지정이 없는 경우 모든 버전 지원

## 구성 요소

### 1. ApiVersion 어노테이션

컨트롤러 클래스나 메서드에 적용하여 지원하는 API 버전을 지정합니다. 어노테이션을 사용하지 않는 경우, 해당 엔드포인트는 모든 버전에서 접근 가능합니다.

```java
@RestController
@RequestMapping("/api/messages")
@ApiVersion({1, 2})  // v1과 v2 API 버전 모두 지원
public class MessageController {
    // ...
}

@RestController
@RequestMapping("/api/common")
// @ApiVersion 어노테이션 없음 - 모든 버전에서 접근 가능
public class CommonController {
    // 이 컨트롤러는 /api/common과 /api/v{any}/common 모두에서 접근 가능
    // 예: /api/common, /api/v1/common, /api/v2/common, /api/v3/common 등
}
```

### 2. ApiVersionRMHM

Spring의 RequestMappingHandlerMapping을 확장하여 API 버전 정보를 URL 경로에 추가합니다.

### 3. WebConfig 설정

WebMvcRegistrations 인터페이스를 구현하여 커스텀 핸들러 매핑 등록

## 사용 방법

### 버전 관리 방식

1. **버전 지정이 필요한 경우**:
```java
@RestController
@RequestMapping("/api/resources")
@ApiVersion({1, 2})  // 이 컨트롤러는 v1과 v2 버전만 지원
public class ResourceController {
    // ...
}
```

2. **모든 버전에서 접근 가능하게 하려는 경우**:
```java
@RestController
@RequestMapping("/api/common")
// @ApiVersion 어노테이션을 사용하지 않음
public class CommonController {
    @GetMapping
    public CommonResponse getCommonData() {
        // 이 메서드는 다음 URL 모두에서 접근 가능:
        // - /api/common
        // - /api/v1/common
        // - /api/v2/common
        // - /api/v{any}/common
        return commonService.getData();
    }
}
```

### 새 컨트롤러에 버전 적용하기

```java
import com.sesac.carematching.config.ApiVersion;

@RestController
@RequestMapping("/api/resources")  // 버전 없는 기본 경로 사용
@ApiVersion({1, 2})  // 지원하는 버전 명시
public class ResourceController {

    @GetMapping
    public List<Resource> getResources() {
        // 이 메서드는 /api/v1/resources와 /api/v2/resources 모두에서 접근 가능
        return resourceService.findAll();
    }
    
    @PostMapping
    @ApiVersion({2})  // 메서드 레벨에서 버전 지정 (v2에서만 사용 가능)
    public Resource createResource(@RequestBody ResourceDto dto) {
        // 이 메서드는 /api/v2/resources에서만 접근 가능
        return resourceService.create(dto);
    }
}
```

### 새 버전 추가하기

새로운 API 버전(v3)을 추가하려면:

1. 기존 컨트롤러에 새 버전 추가:
```java
@ApiVersion({1, 2, 3})  // v3 추가
```

2. 새 버전에서만 사용할 메서드 추가:
```java
@GetMapping("/api/enhanced")
@ApiVersion({3})  // v3에서만 사용 가능
public EnhancedResource getEnhancedResource() {
    // v3 전용 기능
    return resourceService.getEnhanced();
}
```
