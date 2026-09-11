package com.pswied.loan.awscorepayment.orchestrator;

import com.pswied.loan.awscorepayment.domain.Payment;
import com.pswied.loan.awscorepayment.domain.PaymentStatus;
import com.pswied.loan.awscorepayment.service.PaymentService;
import com.pswied.loan.awscorepayment.adapter.PaymentAdapter;
import com.pswied.loan.awscorepayment.ledger.LedgerService;
import com.pswied.loan.awscorepayment.messaging.EventPublisher;
import org.springframework.stereotype.Service;

@Service
public class PaymentOrchestrator {

    private final PaymentService paymentService;
    private final RiskService riskService;
    private final RoutingService routingService;
    private final PaymentAdapter paymentAdapter;
    private final LedgerService ledgerService;
    private final EventPublisher eventPublisher;

    public PaymentOrchestrator(
        PaymentService paymentService,
        RiskService riskService,
        RoutingService routingService,
        PaymentAdapter paymentAdapter,
        LedgerService ledgerService,
        EventPublisher eventPublisher
    ) {
        this.paymentService = paymentService;
        this.riskService = riskService;
        this.routingService = routingService;
        this.paymentAdapter = paymentAdapter;
        this.ledgerService = ledgerService;
        this.eventPublisher = eventPublisher;
    }

    public void processAuthorization(String paymentId) {
        Payment payment = paymentService.getPayment(paymentId);

        // risk check
        boolean allowed = riskService.assess(payment);
        if (!allowed) {
            payment.transitionTo(PaymentStatus.DECLINED);
            eventPublisher.publish("PaymentDeclined", payment.getId());
            return;
        }

        // routing
        String provider = routingService.selectProvider(payment);

        // adapter / network call (mocked)
        boolean success = paymentAdapter.execute(provider, payment);

        if (!success) {
            payment.transitionTo(PaymentStatus.FAILED);
            eventPublisher.publish("PaymentFailed", payment.getId());
            return;
        }

        // ledger posting
        ledgerService.postAuthorization(payment);

        // update status and emit event
        payment.transitionTo(PaymentStatus.AUTHORIZED);
        eventPublisher.publish("PaymentAuthorized", payment.getId());
    }
}
