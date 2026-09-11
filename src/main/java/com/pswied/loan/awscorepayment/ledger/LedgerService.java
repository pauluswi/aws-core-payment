package com.pswied.loan.awscorepayment.ledger;

import com.pswied.loan.awscorepayment.domain.LedgerEntry;
import com.pswied.loan.awscorepayment.domain.LedgerEntry.LedgerDirection;
import com.pswied.loan.awscorepayment.domain.LedgerEntry.LedgerEntryType;
import com.pswied.loan.awscorepayment.domain.MonetaryAmount;
import com.pswied.loan.awscorepayment.domain.Payment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {

    private final LedgerRepository repository;

    public LedgerService(LedgerRepository repository) {
        this.repository = repository;
    }

    public void postAuthorization(Payment payment) {
        // create a debit and credit ledger entry representing the authorization hold
        LedgerEntry debit = new LedgerEntry(payment.getId(), payment.getCustomerId(), LedgerDirection.DEBIT, payment.getAmount(), LedgerEntryType.PAYMENT_AUTHORIZATION);
        LedgerEntry credit = new LedgerEntry(payment.getId(), payment.getMerchantId(), LedgerDirection.CREDIT, payment.getAmount(), LedgerEntryType.PAYMENT_AUTHORIZATION);
        repository.save(debit);
        repository.save(credit);
    }

    public List<LedgerEntry> getEntriesForPayment(String paymentId) {
        return repository.findByPaymentId(paymentId);
    }

    public BigDecimal getBalanceForAccount(String accountId) {
        List<LedgerEntry> entries = repository.findByAccountId(accountId);
        BigDecimal balance = BigDecimal.ZERO;
        for (LedgerEntry e : entries) {
            MonetaryAmount amt = e.getAmount();
            if (e.getDirection() == LedgerDirection.DEBIT) {
                balance = balance.subtract(amt.amount());
            } else {
                balance = balance.add(amt.amount());
            }
        }
        return balance;
    }

    public List<LedgerEntry> allEntries() {
        return repository.findAll();
    }

    public void clear() {
        repository.clear();
    }
}
