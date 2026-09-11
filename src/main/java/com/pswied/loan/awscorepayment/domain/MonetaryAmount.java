package com.pswied.loan.awscorepayment.domain;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record MonetaryAmount(BigDecimal amount, Currency currency) {

    public MonetaryAmount {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
    }

    public static MonetaryAmount of(String amount, String currencyCode) {
        return new MonetaryAmount(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public MonetaryAmount add(MonetaryAmount other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add amounts with different currencies");
        }
        return new MonetaryAmount(amount.add(other.amount), currency);
    }
}
