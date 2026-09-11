package com.pswied.loan.awscorepayment.orchestrator;

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
class RiskServiceTest {

    @Autowired
    private RiskService riskService;

    @Test
    void shouldDeclineBlockedMerchant() {
        Payment p = new Payment("bad-merchant-1","c","ref", new MonetaryAmount(new BigDecimal("10.00"), Currency.getInstance("USD")), "CARD", "WEB", "idem-1");
        assertFalse(riskService.assess(p));
    }

    @Test
    void shouldDeclineOverThreshold() {
        Payment p = new Payment("m","c","ref", new MonetaryAmount(new BigDecimal("20000.00"), Currency.getInstance("USD")), "CARD", "WEB", "idem-2");
        assertFalse(riskService.assess(p));
    }

    @Test
    void shouldApproveNormalPayment() {
        Payment p = new Payment("good-merchant","c","ref", new MonetaryAmount(new BigDecimal("50.00"), Currency.getInstance("USD")), "CARD", "WEB", "idem-3");
        assertTrue(riskService.assess(p));
    }
}
