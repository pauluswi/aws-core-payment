package com.pswied.loan.awscorepayment.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreatePaymentRequest(
    @NotBlank(message = "merchantId is required") String merchantId,
    @NotBlank(message = "customerId is required") String customerId,
    @NotBlank(message = "reference is required") String reference,
    @NotNull(message = "amount is required") BigDecimal amount,
    @NotBlank(message = "currency is required") String currency,
    @NotBlank(message = "paymentMethod is required") String paymentMethod,
    @NotBlank(message = "channel is required") String channel,
    @NotBlank(message = "idempotencyKey is required") String idempotencyKey
) {}
