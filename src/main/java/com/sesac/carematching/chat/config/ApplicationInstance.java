package com.sesac.carematching.chat.config;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ApplicationInstance {
    // 인스턴스가 생성될 때 즉시 UUID를 생성해 race condition 방지
    private final String instanceId = UUID.randomUUID().toString();

    public String getInstanceId() {
        return instanceId;
    }
}
