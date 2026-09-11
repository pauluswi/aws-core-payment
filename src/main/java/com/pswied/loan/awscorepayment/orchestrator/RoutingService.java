package com.pswied.loan.awscorepayment.orchestrator;

import com.pswied.loan.awscorepayment.domain.Payment;
import org.springframework.stereotype.Service;

@Service
public class RoutingService {

    public String selectProvider(Payment payment) {
        // simple placeholder routing logic: pick "VENDOR_A" for CARD, else "VENDOR_B"
        if ("CARD".equalsIgnoreCase(payment.getPaymentMethod())) {
            return "VENDOR_A";
        }
        return "VENDOR_B";
    }
}
