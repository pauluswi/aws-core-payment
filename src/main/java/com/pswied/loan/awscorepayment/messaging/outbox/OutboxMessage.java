package com.pswied.loan.awscorepayment.messaging.outbox;

import java.time.Instant;
import java.util.UUID;

public class OutboxMessage {
    private final String id;
    private final String topic;
    private final String key;
    private final String payload;
    private final Instant createdAt;
    private boolean published;

    public OutboxMessage(String topic, String key, String payload) {
        this.id = UUID.randomUUID().toString();
        this.topic = topic;
        this.key = key;
        this.payload = payload;
        this.createdAt = Instant.now();
        this.published = false;
    }

    public String getId() { return id; }
    public String getTopic() { return topic; }
    public String getKey() { return key; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isPublished() { return published; }
    public void markPublished() { this.published = true; }
}
