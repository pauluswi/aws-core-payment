package com.pswied.loan.awscorepayment.config;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RiskConfig {

    // maximum amount allowed for automatic approval
    private BigDecimal autoApproveMax = new BigDecimal("10000.00");

    // merchant ids to block
    private List<String> blockedMerchants = List.of("bad-merchant-1", "bad-merchant-2");

    // blocked payment methods
    private List<String> blockedPaymentMethods = List.of("STOLEN_CARD");

    public BigDecimal getAutoApproveMax() {
        return autoApproveMax;
    }

    public List<String> getBlockedMerchants() {
        return blockedMerchants;
    }

    public List<String> getBlockedPaymentMethods() {
        return blockedPaymentMethods;
    }
}
