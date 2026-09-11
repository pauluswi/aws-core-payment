package com.pswied.loan.awscorepayment.messaging.backbone;

import java.util.List;

public interface EventBackbone {
    void publishEvent(String topic, String key, String payload);
    List<String> getPublishedEvents();
}
