package com.pswied.loan.awscorepayment.service;

import com.pswied.loan.awscorepayment.api.dto.CreatePaymentRequest;
import com.pswied.loan.awscorepayment.domain.MonetaryAmount;
import com.pswied.loan.awscorepayment.domain.Payment;
import java.util.Currency;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final Map<String, Payment> payments = new ConcurrentHashMap<>();
    private final Map<String, String> idempotencyIndex = new ConcurrentHashMap<>();

    public Payment createPayment(CreatePaymentRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String idempotencyKey = request.idempotencyKey();
        if (idempotencyKey != null && idempotencyIndex.containsKey(idempotencyKey)) {
            String existingPaymentId = idempotencyIndex.get(idempotencyKey);
            return payments.get(existingPaymentId);
        }

        Payment payment = new Payment(
            request.merchantId(),
            request.customerId(),
            request.reference(),
            new MonetaryAmount(request.amount(), Currency.getInstance(request.currency())),
            request.paymentMethod(),
            request.channel(),
            request.idempotencyKey()
        );

        payments.put(payment.getId(), payment);
        idempotencyIndex.put(request.idempotencyKey(), payment.getId());
        return payment;
    }

    public Payment getPayment(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("payment id must not be blank");
        }
        Payment payment = payments.get(id);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found: " + id);
        }
        return payment;
    }
}
