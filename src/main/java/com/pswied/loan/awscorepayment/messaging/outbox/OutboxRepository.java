package com.pswied.loan.awscorepayment.messaging.outbox;

import java.util.List;

public interface OutboxRepository {
    OutboxMessage save(OutboxMessage m);
    List<OutboxMessage> findAllUnpublished();
    List<OutboxMessage> findAll();
    void clear();
}
