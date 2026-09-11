package com.pswied.loan.awscorepayment.orchestrator;

import com.pswied.loan.awscorepayment.domain.Payment;
import org.springframework.stereotype.Service;

@Service
public class RiskService {

    public boolean assess(Payment payment) {
        // simple placeholder: allow all payments under 10,000 in amount
        try {
            return payment.getAmount().amount().doubleValue() < 10_000.00;
        } catch (Exception e) {
            return false;
        }
    }
}
