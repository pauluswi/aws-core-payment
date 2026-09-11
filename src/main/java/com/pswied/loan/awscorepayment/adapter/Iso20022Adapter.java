package com.pswied.loan.awscorepayment.adapter;

import com.pswied.loan.awscorepayment.domain.Payment;
import org.springframework.stereotype.Component;

/**
 * Simple ISO 20022 adapter mock.
 * Builds a minimal XML-like payload and simulates a provider response.
 */
@Component
public class Iso20022Adapter {

    public String formatMessage(Payment payment) {
        return String.format("<Document><PmtId>%s</PmtId><Amt currency=\"%s\">%s</Amt></Document>",
            sanitize(payment.getReference()),
            payment.getAmount().currency().getCurrencyCode(),
            payment.getAmount().amount().toPlainString()
        );
    }

    public boolean parseResponse(String response) {
        if (response == null) return false;
        return response.contains("<Status>ACCP</Status>");
    }

    public String send(Payment payment) {
        if (payment.getReference() != null && payment.getReference().contains("fail")) {
            return "<Document><Status>RJCT</Status></Document>";
        }
        return "<Document><Status>ACCP</Status></Document>";
    }

    private String sanitize(String s) {
        return s == null ? "" : s.replaceAll("[<>]","_");
    }
}
