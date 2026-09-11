package com.pswied.loan.awscorepayment.messaging.backbone;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InMemoryEventBackbone implements EventBackbone {

    private final List<String> events = new ArrayList<>();

    @Override
    public void publishEvent(String topic, String key, String payload) {
        events.add(topic + ":" + key + ":" + payload);
    }

    @Override
    public List<String> getPublishedEvents() {
        return List.copyOf(events);
    }

    public void clear() {
        events.clear();
    }
}
