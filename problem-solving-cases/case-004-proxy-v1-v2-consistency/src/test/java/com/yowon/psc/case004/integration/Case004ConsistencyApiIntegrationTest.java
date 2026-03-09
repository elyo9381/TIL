package com.yowon.psc.case004.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yowon.psc.case004.support.RedisIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "app.seed.enabled=true",
    "app.seed.resource-count=12",
    "app.v1.artificial-race-delay-ms=8",
    "app.repro.default-domain=domain-a"
})
@AutoConfigureMockMvc
class Case004ConsistencyApiIntegrationTest extends RedisIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void reportApiShouldReturnOnlyCoreConsistencyMetrics() throws Exception {
        mockMvc.perform(get("/api/case-004/proxy/repro/report")
                .param("profile", "v1-legacy")
                .param("domain", "domain-a")
                .param("concurrency", "120")
                .param("poolSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.profile").value("v1-legacy"))
            .andExpect(jsonPath("$.duplicateLeaseCount").isNumber())
            .andExpect(jsonPath("$.orphanLeaseCount").isNumber())
            .andExpect(jsonPath("$.idempotencyMismatchCount").isNumber())
            .andExpect(jsonPath("$.avgLatencyMs").doesNotExist())
            .andExpect(jsonPath("$.p95LatencyMs").doesNotExist())
            .andExpect(jsonPath("$.maxLatencyMs").doesNotExist())
            .andExpect(jsonPath("$.successCount").doesNotExist())
            .andExpect(jsonPath("$.failCount").doesNotExist());
    }

    @Test
    void compareApiShouldBeRemoved() throws Exception {
        mockMvc.perform(get("/api/case-004/proxy/repro/compare"))
            .andExpect(status().is4xxClientError());
    }
}
