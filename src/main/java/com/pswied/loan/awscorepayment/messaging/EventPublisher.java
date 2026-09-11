package com.pswied.loan.awscorepayment.messaging;

import com.pswied.loan.awscorepayment.messaging.backbone.EventBackbone;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventPublisher {

    private final OutboxService outboxService;
    private final EventBackbone backbone;

    public EventPublisher(OutboxService outboxService, EventBackbone backbone) {
        this.outboxService = outboxService;
        this.backbone = backbone;
    }

    public void publish(String eventType, String payload) {
        // store to outbox and publish via backbone (mock immediate publish)
        outboxService.storeEvent(eventType, payload, payload);
    }

    public List<String> getPublishedEvents() {
        return backbone.getPublishedEvents();
    }

    public void clear() {
        outboxService.clear();
    }
}
