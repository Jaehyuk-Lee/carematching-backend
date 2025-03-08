package com.sesac.carematching.chat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // ✅ Redis 기반 메시지 브로커를 사용하려면 "/topic"과 "/queue"를 활성화
        config.enableSimpleBroker("/topic", "/queue", "/notifications");

        // ✅ 클라이언트가 메시지를 보낼 때 "/app" prefix를 사용하도록 설정
        config.setApplicationDestinationPrefixes("/app");

        // ✅ Redis를 메시지 브로커로 사용하려면 아래 코드 추가 (선택 사항)
        /*config.setUserDestinationPrefix("/user");*/
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ 클라이언트가 WebSocket을 연결하는 엔드포인트
        registry.addEndpoint("/ws")

            .setAllowedOrigins("https://d12hp6zm8su88f.cloudfront.net/",
                    "https://carematching.net/", "https://www.carematching.net/",
                    "http://localhost:3000", "http://localhost") // 프론트엔드 도메인 허용
            .withSockJS(); // SockJS 사용 시 필요
    }
}
