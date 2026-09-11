package com.pswied.loan.awscorepayment.messaging;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    private final List<String> events = new ArrayList<>();

    public void publish(String eventType, String payload) {
        // simple in-memory event sink for demo and tests
        events.add(eventType + ":" + payload);
    }

    public List<String> getPublishedEvents() {
        return List.copyOf(events);
    }

    public void clear() {
        events.clear();
    }
}
