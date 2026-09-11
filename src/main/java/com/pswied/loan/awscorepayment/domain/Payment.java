package com.pswied.loan.awscorepayment.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Payment {

    private final String id;
    private final String merchantId;
    private final String customerId;
    private final String reference;
    private final MonetaryAmount amount;
    private final String paymentMethod;
    private final String channel;
    private final String idempotencyKey;
    private PaymentStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public Payment(
        String merchantId,
        String customerId,
        String reference,
        MonetaryAmount amount,
        String paymentMethod,
        String channel,
        String idempotencyKey
    ) {
        this.id = UUID.randomUUID().toString();
        this.merchantId = Objects.requireNonNull(merchantId, "merchantId must not be null");
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        this.reference = Objects.requireNonNull(reference, "reference must not be null");
        this.amount = Objects.requireNonNull(amount, "amount must not be null");
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "paymentMethod must not be null");
        this.channel = Objects.requireNonNull(channel, "channel must not be null");
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey must not be null");
        this.status = PaymentStatus.INITIATED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void transitionTo(PaymentStatus nextStatus) {
        Objects.requireNonNull(nextStatus, "nextStatus must not be null");
        if (this.status == nextStatus) {
            return;
        }
        this.status = nextStatus;
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getReference() {
        return reference;
    }

    public MonetaryAmount getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getChannel() {
        return channel;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
