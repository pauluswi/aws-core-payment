package com.pswied.loan.awscorepayment.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class LedgerEntry {

    private final String id;
    private final String paymentId;
    private final String accountId;
    private final LedgerDirection direction;
    private final MonetaryAmount amount;
    private final LedgerEntryType entryType;
    private final Instant createdAt;

    public LedgerEntry(
        String paymentId,
        String accountId,
        LedgerDirection direction,
        MonetaryAmount amount,
        LedgerEntryType entryType
    ) {
        this.id = UUID.randomUUID().toString();
        this.paymentId = Objects.requireNonNull(paymentId, "paymentId must not be null");
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.direction = Objects.requireNonNull(direction, "direction must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.entryType = Objects.requireNonNull(entryType, "entryType must not be null");
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getAccountId() {
        return accountId;
    }

    public LedgerDirection getDirection() {
        return direction;
    }

    public MonetaryAmount getAmount() {
        return amount;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public enum LedgerDirection {
        DEBIT,
        CREDIT
    }

    public enum LedgerEntryType {
        PAYMENT_AUTHORIZATION,
        PAYMENT_CAPTURE,
        PAYMENT_SETTLEMENT,
        PAYMENT_REVERSAL
    }
}
