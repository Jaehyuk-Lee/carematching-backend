package com.sesac.carematching.chat.config;

import com.sesac.carematching.chat.service.NotificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("127.0.0.1", 6379); // Redis 서버와 연결
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory); // ✅ StringRedisTemplate 빈 등록
    }

    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("chat_notifications"); // Redis Pub/Sub 채널 설정
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
                                                        MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, topic()); // 메시지 리스너 등록
        return container;
    }

    @Bean
    public MessageListenerAdapter messageListener(NotificationService notificationService) {
        return new MessageListenerAdapter(notificationService, "onMessage"); // ✅ 수정: 정확한 메서드 이름 전달
    }

}
