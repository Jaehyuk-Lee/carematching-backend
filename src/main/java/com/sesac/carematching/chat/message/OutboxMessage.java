package com.sesac.carematching.chat.message;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "chat_outbox")
public class OutboxMessage {
    @Id
    private String id;

    private String messageId; // reference to chat_messages.id
    private String channel;
    private String payload;
    private String status; // PENDING, SENT, FAILED
    private int attempts;
    private Instant createdAt;

    public OutboxMessage() {
        this.createdAt = Instant.now();
        this.status = "PENDING";
        this.attempts = 0;
    }

    // getters/setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
