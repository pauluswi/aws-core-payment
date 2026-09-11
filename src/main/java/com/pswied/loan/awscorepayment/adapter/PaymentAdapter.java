package com.pswied.loan.awscorepayment.adapter;

import com.pswied.loan.awscorepayment.domain.Payment;
import org.springframework.stereotype.Service;

@Service
public class PaymentAdapter {

    public boolean execute(String provider, Payment payment) {
        // Mock network interaction: always succeed for demo, but fail for specific reference
        if (payment.getReference().contains("fail")) {
            return false;
        }
        // Simulate provider interaction delay lightly omitted for tests
        return true;
    }
}
