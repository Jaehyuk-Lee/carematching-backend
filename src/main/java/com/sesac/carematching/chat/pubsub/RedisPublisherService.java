package com.sesac.carematching.chat.pubsub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPublisherService {
    private final StringRedisTemplate redisTemplate;

    public void publish(String channel, String message) {
        // 3회 시도 모두 실패할 경우 RuntimeException 던짐
        int attempts = 0;
        while (attempts < 3) {
            try {
                redisTemplate.convertAndSend(channel, message);
                return;
            } catch (Exception e) {
                attempts++;
                log.warn("Redis publish 실패 {}/3 | {}: {}", attempts, channel, e.getMessage());
                if (attempts >= 3) {
                    log.error("Redis publish 실패 3/3 | {}: {}", channel, e.getMessage());
                }
                try { TimeUnit.MILLISECONDS.sleep(200L * attempts); } catch (InterruptedException ignored) {}
            }
        }
    }
}
