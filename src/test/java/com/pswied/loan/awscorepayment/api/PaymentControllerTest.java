package com.pswied.loan.awscorepayment.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreatePayment() throws Exception {
        String requestJson = """
            {
              "merchantId": "merchant-001",
              "customerId": "customer-001",
              "reference": "ref-001",
              "amount": 100.00,
              "currency": "USD",
              "paymentMethod": "CARD",
              "channel": "WEB",
              "idempotencyKey": "idem-001"
            }
            """;

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.merchantId").value("merchant-001"))
            .andExpect(jsonPath("$.status").value("INITIATED"))
            .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void shouldGetPaymentById() throws Exception {
        String requestJson = """
            {
              "merchantId": "merchant-002",
              "customerId": "customer-002",
              "reference": "ref-002",
              "amount": 250.50,
              "currency": "USD",
              "paymentMethod": "BANK_TRANSFER",
              "channel": "MOBILE",
              "idempotencyKey": "idem-002"
            }
            """;

        String response = mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        String paymentId = response.substring(response.indexOf("\"id\":\"") + 6, response.indexOf("\"id\":\"") + 6 + 36);

        mockMvc.perform(get("/api/payments/{paymentId}", paymentId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(paymentId))
            .andExpect(jsonPath("$.reference").value("ref-002"));
    }
}
