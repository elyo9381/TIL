package com.yowon.psc.case001.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.yowon.psc.case001.support.MySqlIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
    properties = {
        "app.seed.enabled=true",
        "app.seed.agno=10",
        "app.seed.route-count=6",
        "app.seed.reservation-count=40",
        "app.seed.random-seed=20260228"
    }
)
@AutoConfigureMockMvc
class Case001ApiMetricIntegrationTest extends MySqlIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiShouldReportRealSqlCountPerRequest() throws Exception {
        MvcResult baseline = mockMvc.perform(get("/api/case-001/reservations/baseline")
                .param("agno", "10")
                .param("limit", "20"))
            .andExpect(status().isOk())
            .andReturn();

        MvcResult optimized = mockMvc.perform(get("/api/case-001/reservations/optimized")
                .param("agno", "10")
                .param("limit", "20"))
            .andExpect(status().isOk())
            .andReturn();

        String baselineBody = baseline.getResponse().getContentAsString();
        String optimizedBody = optimized.getResponse().getContentAsString();

        int baselineSampleCount = JsonPath.read(baselineBody, "$.metrics.sampleCount");
        int optimizedSampleCount = JsonPath.read(optimizedBody, "$.metrics.sampleCount");
        int baselineSqlCount = JsonPath.read(baselineBody, "$.metrics.totalSqlCount");
        int optimizedSqlCount = JsonPath.read(optimizedBody, "$.metrics.totalSqlCount");

        assertThat(baselineSampleCount).isEqualTo(1);
        assertThat(optimizedSampleCount).isEqualTo(1);
        assertThat(baselineSqlCount).isGreaterThan(optimizedSqlCount);
        assertThat(optimizedSqlCount).isPositive();
    }
}
