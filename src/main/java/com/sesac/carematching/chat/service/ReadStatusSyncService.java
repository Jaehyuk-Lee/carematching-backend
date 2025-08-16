package com.sesac.carematching.chat.service;

import com.sesac.carematching.chat.message.ChatReadStatus;
import com.sesac.carematching.chat.message.ChatReadStatusRepository;
import com.sesac.carematching.chat.message.Message;
import com.sesac.carematching.chat.message.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReadStatusSyncService {
    private static final Logger log = LoggerFactory.getLogger(ReadStatusSyncService.class);

    private final StringRedisTemplate redisTemplate;
    private final ChatReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    private static final String READ_KEY_FORMAT = "chat:read:%s:%s";
    private static final String UPDATED_SET = "chat:read:updated";
    private static final String DEAD_SET = "chat:read:dead";

    // 매 5분마다 실행
    @Scheduled(fixedDelayString = "PT5M")
    public void syncReadStatusToMongo() {
        log.info("Start syncReadStatusToMongo (ZPOP_MIN)");
        try {
            int batch = 500;
            while (true) {
                java.util.Set<ZSetOperations.TypedTuple<String>> popped = redisTemplate.opsForZSet().popMin("chat:read:updated", batch);
                if (popped == null || popped.isEmpty()) break;
                for (ZSetOperations.TypedTuple<String> t : popped) {
                    String member = t.getValue();
                    Double score = t.getScore();
                    try {
                        if (member == null || member.trim().isEmpty()) {
                            log.warn("Skipping empty member from {}", UPDATED_SET);
                            continue;
                        }
                        String[] parts = member.split(":");
                        if (parts.length < 2) {
                            log.warn("Malformed member {} moving to dead set", member);
                            redisTemplate.opsForZSet().remove(UPDATED_SET, member);
                            redisTemplate.opsForZSet().add(DEAD_SET, member, score == null ? System.currentTimeMillis() : score);
                            continue;
                        }
                        String roomId = parts[0];
                        String userId = parts[1];

                        String key = String.format(READ_KEY_FORMAT, roomId, String.valueOf(userId));
                        String value = redisTemplate.opsForValue().get(key);
                        if (value == null) continue;

                        ChatReadStatus status = readStatusRepository.findByRoomIdAndUserId(roomId, userId)
                            .orElseGet(() -> {
                                ChatReadStatus crs = new ChatReadStatus();
                                crs.setRoomId(roomId);
                                crs.setUserId(userId);
                                return crs;
                            });

                        boolean shouldSave = false;
                        try {
                            long epoch = Long.parseLong(value);
                            Instant newReadAt = Instant.ofEpochMilli(epoch);
                            Instant existing = status.getReadAt();
                            if (existing == null || newReadAt.isAfter(existing)) {
                                status.setReadAt(newReadAt);
                                shouldSave = true;
                            }
                        } catch (NumberFormatException e) {
                            // legacy or malformed value — move to dead-letter to avoid infinite retry
                            log.warn("Non-epoch value for key {} member {} moved to dead set: {}", key, member, value);
                            redisTemplate.opsForZSet().add(DEAD_SET, member, score == null ? System.currentTimeMillis() : score);
                            continue;
                        }

                        if (shouldSave) readStatusRepository.save(status);
                    } catch (Exception e) {
                        log.warn("Failed to process popped member {}: {}", member, e.getMessage());
                        // unexpected failure (DB down etc.) — re-add for retry
                        double s = score == null ? System.currentTimeMillis() : score;
                        redisTemplate.opsForZSet().add(UPDATED_SET, member, s);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Sync failed: {}", e.getMessage());
        }
        log.info("End syncReadStatusToMongo (ZPOP_MIN)");
    }
}
