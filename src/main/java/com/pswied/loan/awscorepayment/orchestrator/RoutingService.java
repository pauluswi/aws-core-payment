package com.pswied.loan.awscorepayment.orchestrator;

import com.pswied.loan.awscorepayment.config.RoutingConfig;
import com.pswied.loan.awscorepayment.domain.Payment;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RoutingService {

    private final RoutingConfig config;

    public RoutingService(RoutingConfig config) {
        this.config = config;
    }

    public String selectProvider(Payment payment) {
        if (payment == null) return config.getDefaultProvider();
        String method = payment.getPaymentMethod();
        if (method == null) return config.getDefaultProvider();

        List<String> providers = config.getMethodToProviders().get(method.toUpperCase());
        if (providers == null || providers.isEmpty()) {
            return config.getDefaultProvider();
        }

        // pick first available provider — in real system consider health/weights
        return providers.get(0);
    }
}
