package com.pswied.loan.awscorepayment.orchestrator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pswied.loan.awscorepayment.domain.MonetaryAmount;
import com.pswied.loan.awscorepayment.domain.Payment;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RoutingServiceTest {

    @Autowired
    private RoutingService routingService;

    @Test
    void shouldRouteCardToVendorA() {
        Payment p = new Payment("m","c","ref", new MonetaryAmount(new BigDecimal("10.00"), Currency.getInstance("USD")), "CARD", "WEB", "idem-r1");
        assertEquals("VENDOR_A", routingService.selectProvider(p));
    }

    @Test
    void shouldRouteBankTransferToVendorB() {
        Payment p = new Payment("m","c","ref", new MonetaryAmount(new BigDecimal("10.00"), Currency.getInstance("USD")), "BANK_TRANSFER", "ACH", "idem-r2");
        assertEquals("VENDOR_B", routingService.selectProvider(p));
    }

    @Test
    void unknownMethodGoesToDefault() {
        Payment p = new Payment("m","c","ref", new MonetaryAmount(new BigDecimal("5.00"), Currency.getInstance("USD")), "CRYPTO", "WEB", "idem-r3");
        assertEquals("VENDOR_B", routingService.selectProvider(p));
    }
}
