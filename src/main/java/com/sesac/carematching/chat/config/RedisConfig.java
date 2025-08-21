package com.sesac.carematching.chat.config;

import com.sesac.carematching.chat.pubsub.ChatMessageSubscriber;
import com.sesac.carematching.chat.pubsub.NotificationSubscriber;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration server = new RedisStandaloneConfiguration(redisHost, redisPort);

        LettuceClientConfiguration client = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(2))
            .clientOptions(ClientOptions.builder()
                .autoReconnect(true)
                .socketOptions(SocketOptions.builder()
                    .connectTimeout(Duration.ofSeconds(1))
                    .build())
                .build())
            .shutdownTimeout(Duration.ofMillis(300))
            .build();

        return new LettuceConnectionFactory(server, client);
    }

    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory); // ✅ StringRedisTemplate 빈 등록
    }

    @Bean
    public ChannelTopic notificationTopic() {
        // Notification - 단일 채널 설정
        return new ChannelTopic("chat_notifications");
    }

    @Bean
    public PatternTopic chatRoomTopic() {
        // Chat Room - chat_room_* 패턴을 통해 모든 채팅 채널 수신 (여러 채널을 한 번에 구독)
        return new PatternTopic("chat_room_*");
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
                                                        NotificationSubscriber notificationSubscriber,
                                                        ChatMessageSubscriber chatMessageSubscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(notificationSubscriber, notificationTopic());
        container.addMessageListener(chatMessageSubscriber, chatRoomTopic());
        return container;
    }

}
