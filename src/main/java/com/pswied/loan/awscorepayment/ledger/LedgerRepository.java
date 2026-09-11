package com.pswied.loan.awscorepayment.ledger;

import com.pswied.loan.awscorepayment.domain.LedgerEntry;
import java.util.List;

public interface LedgerRepository {
    LedgerEntry save(LedgerEntry entry);
    List<LedgerEntry> findByPaymentId(String paymentId);
    List<LedgerEntry> findByAccountId(String accountId);
    List<LedgerEntry> findAll();
    void clear();
}
