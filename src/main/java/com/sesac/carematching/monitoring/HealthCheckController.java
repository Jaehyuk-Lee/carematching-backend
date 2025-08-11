package com.sesac.carematching.monitoring;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/health")
public class HealthCheckController {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", new Date());

        // 시스템 정보
        Runtime runtime = Runtime.getRuntime();
        response.put("memory", Map.of(
            "total", runtime.totalMemory(),
            "free", runtime.freeMemory(),
            "used", runtime.totalMemory() - runtime.freeMemory()
        ));

        // Resilience4j CircuitBreaker 상태 수집
        Map<String, String> circuitBreakers = new HashMap<>();
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb ->
            circuitBreakers.put(cb.getName(), cb.getState().name())
        );
        response.put("circuitBreakers", circuitBreakers);

        return ResponseEntity.ok(response);
    }
}
