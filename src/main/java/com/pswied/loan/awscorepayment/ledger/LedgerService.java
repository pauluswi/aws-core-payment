package com.pswied.loan.awscorepayment.ledger;

import com.pswied.loan.awscorepayment.domain.LedgerEntry;
import com.pswied.loan.awscorepayment.domain.LedgerEntry.LedgerDirection;
import com.pswied.loan.awscorepayment.domain.LedgerEntry.LedgerEntryType;
import com.pswied.loan.awscorepayment.domain.Payment;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {

    private final List<LedgerEntry> entries = new ArrayList<>();

    public void postAuthorization(Payment payment) {
        // For demo: create a debit and credit ledger entry representing the authorization hold
        LedgerEntry debit = new LedgerEntry(payment.getId(), payment.getCustomerId(), LedgerDirection.DEBIT, payment.getAmount(), LedgerEntryType.PAYMENT_AUTHORIZATION);
        LedgerEntry credit = new LedgerEntry(payment.getId(), payment.getMerchantId(), LedgerDirection.CREDIT, payment.getAmount(), LedgerEntryType.PAYMENT_AUTHORIZATION);
        entries.add(debit);
        entries.add(credit);
    }

    public List<LedgerEntry> getEntriesForPayment(String paymentId) {
        List<LedgerEntry> result = new ArrayList<>();
        for (LedgerEntry e : entries) {
            if (e.getPaymentId().equals(paymentId)) {
                result.add(e);
            }
        }
        return result;
    }
}
