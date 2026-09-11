package com.pswied.loan.awscorepayment.orchestrator;

import com.pswied.loan.awscorepayment.config.RiskConfig;
import com.pswied.loan.awscorepayment.domain.Payment;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class RiskService {

    private final RiskConfig config;

    public RiskService(RiskConfig config) {
        this.config = config;
    }

    public boolean assess(Payment payment) {
        if (payment == null) return false;

        // blocked merchant check
        if (config.getBlockedMerchants().contains(payment.getMerchantId())) {
            return false;
        }

        // blocked payment method
        if (config.getBlockedPaymentMethods().stream().anyMatch(m -> m.equalsIgnoreCase(payment.getPaymentMethod()))) {
            return false;
        }

        // amount threshold
        try {
            BigDecimal amt = payment.getAmount().amount();
            if (amt.compareTo(config.getAutoApproveMax()) > 0) {
                // above auto-approve threshold => fails automated risk check
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
