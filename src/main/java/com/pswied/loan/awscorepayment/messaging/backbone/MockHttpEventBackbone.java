package com.pswied.loan.awscorepayment.messaging.backbone;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * HTTP-backed event backbone that forwards events to an external mock service.
 * Enabled when messaging.backbone=remote
 */
@Component
@Primary
@ConditionalOnProperty(name = "messaging.backbone", havingValue = "remote")
public class MockHttpEventBackbone implements EventBackbone {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String endpoint;

    public MockHttpEventBackbone() {
        // default endpoint for docker-compose mock
        this.endpoint = System.getProperty("mock.backbone.url", System.getenv().getOrDefault("MOCK_BACKBONE_URL", "http://mock-backbone:8081"));
    }

    @Override
    public void publishEvent(String topic, String key, String payload) {
        try {
            String url = endpoint + "/publish";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String body = String.format("{\"topic\":\"%s\",\"key\":\"%s\",\"payload\":\"%s\"}", topic, key, payload.replaceAll("\"","\\\""));
            HttpEntity<String> ent = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, ent, String.class);
        } catch (Exception ex) {
            // swallow for mock resilience
        }
    }

    @Override
    public List<String> getPublishedEvents() {
        try {
            String url = endpoint + "/events";
            String resp = restTemplate.getForObject(url, String.class);
            if (resp == null || resp.isBlank()) return Collections.emptyList();
            // simple split by newline
            return List.of(resp.split("\\n"));
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
