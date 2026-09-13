package com.pswied.loan.awscorepayment.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CorrelationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAddCorrelationHeaderWhenMissing() throws Exception {
        var res = mockMvc.perform(get("/api/health")).andExpect(status().isOk()).andReturn().getResponse();
        String hdr = res.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
        assertThat(hdr).isNotNull();
        assertThat(hdr).isNotEmpty();
    }
}
