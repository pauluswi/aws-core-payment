package com.pswied.loan.awscorepayment.adapter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pswied.loan.awscorepayment.domain.MonetaryAmount;
import com.pswied.loan.awscorepayment.domain.Payment;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdapterIntegrationTest {

    @Autowired
    private PaymentAdapter paymentAdapter;

    @Test
    void iso8583ShouldApprove() {
        Payment p = new Payment("m","c","ref-iso8583", new MonetaryAmount(new BigDecimal("10.00"), Currency.getInstance("USD")), "CARD", "WEB", "idem-iso1");
        assertTrue(paymentAdapter.execute("ISO8583", p));
    }

    @Test
    void iso8583ShouldDeclineOnFailRef() {
        Payment p = new Payment("m","c","ref-fail-iso8583", new MonetaryAmount(new BigDecimal("10.00"), Currency.getInstance("USD")), "CARD", "WEB", "idem-iso2");
        assertFalse(paymentAdapter.execute("ISO8583", p));
    }

    @Test
    void iso20022ShouldApprove() {
        Payment p = new Payment("m","c","ref-iso20022", new MonetaryAmount(new BigDecimal("20.00"), Currency.getInstance("USD")), "BANK_TRANSFER", "ACH", "idem-iso2001");
        assertTrue(paymentAdapter.execute("ISO20022", p));
    }

    @Test
    void iso20022ShouldRejectOnFailRef() {
        Payment p = new Payment("m","c","ref-fail-iso20022", new MonetaryAmount(new BigDecimal("20.00"), Currency.getInstance("USD")), "BANK_TRANSFER", "ACH", "idem-iso2002");
        assertFalse(paymentAdapter.execute("ISO20022", p));
    }
}
