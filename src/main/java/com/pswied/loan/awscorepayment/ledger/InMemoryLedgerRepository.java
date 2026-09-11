package com.pswied.loan.awscorepayment.ledger;

import com.pswied.loan.awscorepayment.domain.LedgerEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLedgerRepository implements LedgerRepository {

    private final List<LedgerEntry> store = new CopyOnWriteArrayList<>();

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        store.add(entry);
        return entry;
    }

    @Override
    public List<LedgerEntry> findByPaymentId(String paymentId) {
        List<LedgerEntry> result = new ArrayList<>();
        for (LedgerEntry e : store) {
            if (e.getPaymentId().equals(paymentId)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<LedgerEntry> findByAccountId(String accountId) {
        List<LedgerEntry> result = new ArrayList<>();
        for (LedgerEntry e : store) {
            if (e.getAccountId().equals(accountId)) {
                result.add(e);
            }
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public List<LedgerEntry> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store));
    }

    @Override
    public void clear() {
        store.clear();
    }
}
