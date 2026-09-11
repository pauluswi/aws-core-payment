package com.pswied.loan.awscorepayment.messaging.outbox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOutboxRepository implements OutboxRepository {

    private final List<OutboxMessage> store = new CopyOnWriteArrayList<>();

    @Override
    public OutboxMessage save(OutboxMessage m) {
        store.add(m);
        return m;
    }

    @Override
    public List<OutboxMessage> findAllUnpublished() {
        List<OutboxMessage> res = new ArrayList<>();
        for (OutboxMessage m : store) {
            if (!m.isPublished()) res.add(m);
        }
        return List.copyOf(res);
    }

    @Override
    public List<OutboxMessage> findAll() {
        return List.copyOf(store);
    }

    @Override
    public void clear() {
        store.clear();
    }
}
