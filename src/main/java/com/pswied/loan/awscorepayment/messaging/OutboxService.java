package com.pswied.loan.awscorepayment.messaging;

import com.pswied.loan.awscorepayment.messaging.outbox.OutboxMessage;
import com.pswied.loan.awscorepayment.messaging.outbox.OutboxRepository;
import com.pswied.loan.awscorepayment.messaging.backbone.EventBackbone;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OutboxService {

    private final OutboxRepository repository;
    private final EventBackbone backbone;

    public OutboxService(OutboxRepository repository, EventBackbone backbone) {
        this.repository = repository;
        this.backbone = backbone;
    }

    public OutboxMessage storeEvent(String topic, String key, String payload) {
        OutboxMessage m = new OutboxMessage(topic, key, payload);
        repository.save(m);
        // For mock environment, publish immediately and mark published
        backbone.publishEvent(topic, key, payload);
        m.markPublished();
        return m;
    }

    public List<OutboxMessage> pending() {
        return repository.findAllUnpublished();
    }

    public List<OutboxMessage> all() {
        return repository.findAll();
    }

    public void clear() {
        repository.clear();
    }
}
