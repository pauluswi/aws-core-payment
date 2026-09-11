package com.pswied.loan.awscorepayment.adapter;

import com.pswied.loan.awscorepayment.domain.Payment;
import org.springframework.stereotype.Component;

/**
 * Simple ISO 8583 adapter mock.
 * Builds a compact ISO8583-like string and simulates a provider response.
 */
@Component
public class Iso8583Adapter {

    public String formatMessage(Payment payment) {
        // very small mock representation of an ISO 8583 message
        return String.format("MTI=0200|PAN=%s|AMT=%s|CUR=%s|REF=%s",
            sanitize(payment.getPaymentMethod()),
            payment.getAmount().amount().toPlainString(),
            payment.getAmount().currency().getCurrencyCode(),
            sanitize(payment.getReference())
        );
    }

    public boolean parseResponse(String response) {
        // treat responses containing 'APPROVED' as success
        if (response == null) return false;
        return response.contains("APPROVED");
    }

    public String send(Payment payment) {
        // mock sending: return APPROVED unless reference contains 'fail'
        if (payment.getReference() != null && payment.getReference().contains("fail")) {
            return "MTI=0210|RESP=DECLINED";
        }
        return "MTI=0210|RESP=APPROVED";
    }

    private String sanitize(String s) {
        return s == null ? "" : s.replaceAll("[|=]","_");
    }
}
