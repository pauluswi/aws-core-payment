package com.pswied.loan.awscorepayment.orchestrator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pswied.loan.awscorepayment.domain.Payment;
import com.pswied.loan.awscorepayment.domain.PaymentStatus;
import com.pswied.loan.awscorepayment.ledger.LedgerService;
import com.pswied.loan.awscorepayment.messaging.EventPublisher;
import com.pswied.loan.awscorepayment.service.PaymentService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PaymentOrchestratorTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentOrchestrator orchestrator;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private EventPublisher eventPublisher;

    @BeforeEach
    void beforeEach() {
        eventPublisher.clear();
    }

    @Test
    void shouldAuthorizePaymentHappyPath() {
        Payment payment = paymentService.createPayment(new com.pswied.loan.awscorepayment.api.dto.CreatePaymentRequest(
            "merchant-100",
            "customer-100",
            "ref-100",
            new BigDecimal("50.00"),
            "USD",
            "CARD",
            "WEB",
            "idem-100"
        ));

        orchestrator.processAuthorization(payment.getId());

        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
        assertEquals(2, ledgerService.getEntriesForPayment(payment.getId()).size());
        assertTrue(eventPublisher.getPublishedEvents().stream().anyMatch(e -> e.startsWith("PaymentAuthorized:")));
    }

    @Test
    void shouldDeclineOnRisk() {
        Payment payment = paymentService.createPayment(new com.pswied.loan.awscorepayment.api.dto.CreatePaymentRequest(
            "merchant-101",
            "customer-101",
            "ref-101",
            new BigDecimal("1000000.00"),
            "USD",
            "CARD",
            "WEB",
            "idem-101"
        ));

        orchestrator.processAuthorization(payment.getId());

        assertEquals(PaymentStatus.DECLINED, payment.getStatus());
        assertEquals(0, ledgerService.getEntriesForPayment(payment.getId()).size());
        assertTrue(eventPublisher.getPublishedEvents().stream().anyMatch(e -> e.startsWith("PaymentDeclined:")));
    }

    @Test
    void shouldFailWhenAdapterFails() {
        Payment payment = paymentService.createPayment(new com.pswied.loan.awscorepayment.api.dto.CreatePaymentRequest(
            "merchant-102",
            "customer-102",
            "ref-fail-102",
            new BigDecimal("25.00"),
            "USD",
            "CARD",
            "WEB",
            "idem-102"
        ));

        orchestrator.processAuthorization(payment.getId());

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(0, ledgerService.getEntriesForPayment(payment.getId()).size());
        assertTrue(eventPublisher.getPublishedEvents().stream().anyMatch(e -> e.startsWith("PaymentFailed:")));
    }
}
