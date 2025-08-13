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
        // 인메모리 브로커(SimpleBroker) 활성화 (인스턴스 간 동기화는 Redis Pub/Sub 구현)
        config.enableSimpleBroker("/topic", "/queue", "/notifications");

        // 클라이언트가 메시지를 보낼 때 "/app" prefix를 사용하도록 설정
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket을 연결하는 엔드포인트
        registry.addEndpoint("/ws")

            .setAllowedOrigins("https://d12hp6zm8su88f.cloudfront.net/",
                    "https://carematching.net/", "https://www.carematching.net/",
                    "http://localhost:3000", "http://localhost") // 프론트엔드 도메인 허용
            .withSockJS(); // SockJS 사용 시 필요
    }
}
