package com.pswied.loan.awscorepayment.messaging;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pswied.loan.awscorepayment.domain.MonetaryAmount;
import com.pswied.loan.awscorepayment.domain.Payment;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OutboxIntegrationTest {

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private com.pswied.loan.awscorepayment.messaging.backbone.InMemoryEventBackbone backbone;

    @BeforeEach
    void beforeEach() {
        outboxService.clear();
        backbone.clear();
    }

    @Test
    void shouldStoreAndPublishEvent() {
        Payment p = new Payment("m","c","ref-outbox", new MonetaryAmount(new BigDecimal("12.00"), Currency.getInstance("USD")), "CARD", "WEB", "idem-outbox");
        eventPublisher.publish("PaymentAuthorized", p.getId());

        assertTrue(backbone.getPublishedEvents().stream().anyMatch(e -> e.contains("PaymentAuthorized:")));
        assertEquals(1, outboxService.all().size());
    }
}
