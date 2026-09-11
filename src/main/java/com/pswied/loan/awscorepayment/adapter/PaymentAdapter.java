package com.pswied.loan.awscorepayment.adapter;

import com.pswied.loan.awscorepayment.domain.Payment;
import org.springframework.stereotype.Service;

/**
 * High-level payment adapter that delegates to protocol-specific adapters.
 * Provider strings control which protocol is used. For demo:
 * - "ISO8583" -> Iso8583Adapter
 * - "ISO20022" -> Iso20022Adapter
 * - otherwise -> simple success
 */
@Service
public class PaymentAdapter {

    private final Iso8583Adapter iso8583Adapter;
    private final Iso20022Adapter iso20022Adapter;

    public PaymentAdapter(Iso8583Adapter iso8583Adapter, Iso20022Adapter iso20022Adapter) {
        this.iso8583Adapter = iso8583Adapter;
        this.iso20022Adapter = iso20022Adapter;
    }

    public boolean execute(String provider, Payment payment) {
        if (provider == null) {
            return defaultExecute(payment);
        }
        if (provider.equalsIgnoreCase("ISO8583") || provider.equalsIgnoreCase("VENDOR_A")) {
            String message = iso8583Adapter.formatMessage(payment);
            String resp = iso8583Adapter.send(payment);
            return iso8583Adapter.parseResponse(resp);
        }

        if (provider.equalsIgnoreCase("ISO20022") || provider.equalsIgnoreCase("VENDOR_B")) {
            String message = iso20022Adapter.formatMessage(payment);
            String resp = iso20022Adapter.send(payment);
            return iso20022Adapter.parseResponse(resp);
        }

        return defaultExecute(payment);
    }

    private boolean defaultExecute(Payment payment) {
        if (payment.getReference() != null && payment.getReference().contains("fail")) {
            return false;
        }
        return true;
    }
}
