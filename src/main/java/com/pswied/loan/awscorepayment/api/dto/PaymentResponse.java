package com.pswied.loan.awscorepayment.api.dto;

import com.pswied.loan.awscorepayment.domain.Payment;
import com.pswied.loan.awscorepayment.domain.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
    String id,
    String merchantId,
    String customerId,
    String reference,
    BigDecimal amount,
    String currency,
    String paymentMethod,
    String channel,
    String idempotencyKey,
    PaymentStatus status,
    Instant createdAt,
    Instant updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getMerchantId(),
            payment.getCustomerId(),
            payment.getReference(),
            payment.getAmount().amount(),
            payment.getAmount().currency().getCurrencyCode(),
            payment.getPaymentMethod(),
            payment.getChannel(),
            payment.getIdempotencyKey(),
            payment.getStatus(),
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
    }
}
