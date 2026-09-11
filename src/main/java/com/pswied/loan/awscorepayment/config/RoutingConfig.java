package com.pswied.loan.awscorepayment.config;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RoutingConfig {

    // mapping from payment method -> preferred providers in order
    private Map<String, List<String>> methodToProviders = Map.of(
        "CARD", List.of("VENDOR_A", "ISO8583"),
        "BANK_TRANSFER", List.of("VENDOR_B", "ISO20022")
    );

    private String defaultProvider = "VENDOR_B";

    public Map<String, List<String>> getMethodToProviders() {
        return methodToProviders;
    }

    public String getDefaultProvider() {
        return defaultProvider;
    }
}
