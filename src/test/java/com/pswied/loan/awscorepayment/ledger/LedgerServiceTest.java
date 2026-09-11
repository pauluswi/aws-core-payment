package com.pswied.loan.awscorepayment.ledger;

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
class LedgerServiceTest {

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private LedgerRepository ledgerRepository;

    @BeforeEach
    void beforeEach() {
        ledgerService.clear();
    }

    @Test
    void shouldPostDoubleEntryForAuthorization() {
        Payment payment = new Payment(
            "merchant-ledger",
            "customer-ledger",
            "ref-ledger",
            new MonetaryAmount(new BigDecimal("75.50"), Currency.getInstance("USD")),
            "CARD",
            "WEB",
            "idem-ledger"
        );

        ledgerService.postAuthorization(payment);

        assertEquals(2, ledgerService.getEntriesForPayment(payment.getId()).size());

        // merchant balance should be +75.50, customer -75.50
        assertEquals(new BigDecimal("75.50"), ledgerService.getBalanceForAccount("merchant-ledger"));
        assertEquals(new BigDecimal("-75.50"), ledgerService.getBalanceForAccount("customer-ledger"));
    }
}
