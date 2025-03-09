//package com.sesac.carematching.chat.controller;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.listener.ChannelTopic;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.PostMapping;
//
//@Slf4j
//@RequiredArgsConstructor
//@Controller
//public class NotificationController {
//
//    private final SimpMessagingTemplate messagingTemplate;
//    private final StringRedisTemplate redisTemplate;
//    private final ChannelTopic topic;
//
//
//    // 전체 사용자에게 알림 전송
//    @MessageMapping("/send-notification")
//    @SendTo("/topic/notifications")
//    public String sendNotification(String message) {
//        log.info("Sending notification: {}", message);
//        return message; // 모든 구독자에게 전달
//    }
//
//    // 특정 사용자에게 알림 전송
//    public void sendNotificationToUser(String userId, String message) {
//        String destination = "/queue/notifications/" + userId;
//        log.info("Sending notification to {}: {}", userId, message);
//        messagingTemplate.convertAndSend(destination, message);
//    }
//    @PostMapping("/test")
//    public ResponseEntity<String> sendTestNotification() {
//        String message = "테스트 알림 메시지!";
//        redisTemplate.convertAndSend(topic.getTopic(), "123:" + message);
//        return ResponseEntity.ok("✅ 테스트 알림 전송 완료!");
//    }
//}
