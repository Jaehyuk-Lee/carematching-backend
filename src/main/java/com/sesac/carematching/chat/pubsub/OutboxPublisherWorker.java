package com.sesac.carematching.chat.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.carematching.chat.message.OutboxMessage;
import com.sesac.carematching.chat.message.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxPublisherWorker {
    private final OutboxRepository outboxRepository;
    private final RedisPublisherService redisPublisherService;

    // 5초마다 PENDING 항목 체크하여 발행 시도
    @Scheduled(fixedDelayString = "PT5S")
    public void pollAndPublish() {
        List<OutboxMessage> pending = outboxRepository.findByStatus("PENDING");
        for (OutboxMessage o : pending) {
            try {
                redisPublisherService.publish(o.getChannel(), o.getPayload());
                o.setStatus("SENT");
                outboxRepository.save(o);
            } catch (Exception e) {
                o.setAttempts(o.getAttempts() + 1);
                if (o.getAttempts() >= 5) {
                    o.setStatus("FAILED");
                }
                outboxRepository.save(o);
                log.warn("Outbox publish failed id={} attempts={}", o.getId(), o.getAttempts());
            }
        }
    }
}
