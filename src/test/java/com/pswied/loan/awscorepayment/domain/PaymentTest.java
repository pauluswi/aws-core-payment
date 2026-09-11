package com.pswied.loan.awscorepayment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    void paymentShouldInitializeWithExpectedDefaults() {
        MonetaryAmount amount = new MonetaryAmount(new BigDecimal("100.00"), Currency.getInstance("USD"));

        Payment payment = new Payment(
            "merchant-001",
            "customer-001",
            "ref-001",
            amount,
            "CARD",
            "WEB",
            "idemp-001"
        );

        assertNotNull(payment.getId());
        assertEquals(PaymentStatus.INITIATED, payment.getStatus());
        assertEquals("merchant-001", payment.getMerchantId());
        assertEquals(amount, payment.getAmount());
    }

    @Test
    void paymentShouldTransitionStatus() {
        MonetaryAmount amount = new MonetaryAmount(new BigDecimal("100.00"), Currency.getInstance("USD"));
        Payment payment = new Payment(
            "merchant-001",
            "customer-001",
            "ref-002",
            amount,
            "CARD",
            "WEB",
            "idemp-002"
        );

        payment.transitionTo(PaymentStatus.AUTHORIZED);

        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
        assertNotNull(payment.getUpdatedAt());
    }
}
