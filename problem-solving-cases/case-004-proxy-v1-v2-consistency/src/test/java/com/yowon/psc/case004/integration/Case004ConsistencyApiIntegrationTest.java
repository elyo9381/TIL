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
    void compareApiShouldReturnThreeProfilesAndCoreMetrics() throws Exception {
        mockMvc.perform(get("/api/case-004/proxy/repro/compare")
                .param("domain", "domain-a")
                .param("concurrency", "120")
                .param("iterations", "2")
                .param("poolSize", "10")
                .param("appCount", "1")
                .param("cooldownMinutes", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.domain").value("domain-a"))
            .andExpect(jsonPath("$.v1P3.profile").value("v1-p3"))
            .andExpect(jsonPath("$.v1P4.profile").value("v1-p4"))
            .andExpect(jsonPath("$.v2Target.profile").value("v2-target"))
            .andExpect(jsonPath("$.v1P3.duplicateLeaseCount").isNumber())
            .andExpect(jsonPath("$.v2Target.duplicateLeaseCount").isNumber());
    }
}
