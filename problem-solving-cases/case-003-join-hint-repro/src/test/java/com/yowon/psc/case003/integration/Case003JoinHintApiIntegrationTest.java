package com.yowon.psc.case003.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yowon.psc.case003.support.MySqlIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "app.schema.auto-create=true",
    "app.seed.enabled=true",
    "app.seed.reservation-count=30000"
})
@AutoConfigureMockMvc
class Case003JoinHintApiIntegrationTest extends MySqlIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiShouldReturnExplainPlansForHintOffAndOn() throws Exception {
        mockMvc.perform(get("/api/case-003/join-hint/report")
                .param("serviceType", "VIP")
                .param("limit", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.withoutHintPlan[0].tableName").value("tr"))
            .andExpect(jsonPath("$.withHintPlan[0].tableName").value("r"))
            .andExpect(jsonPath("$.withoutHintResultSize").value(20))
            .andExpect(jsonPath("$.withHintResultSize").value(20));
    }

    @Test
    void benchmarkApiShouldReturnAggregatedLatencyStats() throws Exception {
        mockMvc.perform(get("/api/case-003/join-hint/benchmark")
                .param("serviceType", "VIP")
                .param("limit", "20")
                .param("iterations", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.iterations").value(10))
            .andExpect(jsonPath("$.withoutHint.avgLatencyMs").isNumber())
            .andExpect(jsonPath("$.withHint.avgLatencyMs").isNumber())
            .andExpect(jsonPath("$.withoutHint.avgLatencyMs").value(org.hamcrest.Matchers.greaterThan(0.0)))
            .andExpect(jsonPath("$.withHint.avgLatencyMs").value(org.hamcrest.Matchers.greaterThan(0.0)));
    }
}
