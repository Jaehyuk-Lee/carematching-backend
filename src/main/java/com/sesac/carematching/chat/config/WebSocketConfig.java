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
        config.enableSimpleBroker("/topic", "/queue"); // 메시지 수신용
        config.setApplicationDestinationPrefixes("/app"); // 메시지 발신용
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // CORS 설정을 통해 프론트엔드에서 접근 가능하게 함
        registry.addEndpoint("/ws")
            .setAllowedOrigins("https://d12hp6zm8su88f.cloudfront.net/",
                    "https://carematching.net/", "https://www.carematching.net/",
                    "http://localhost:3000", "http://localhost") // 프론트엔드 도메인 허용
            .withSockJS(); // SockJS 사용 시 필요
    }
}
