package com.yowon.psc.case002.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yowon.psc.case002.support.MySqlIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "app.seed.enabled=true",
    "app.seed.agno=10"
})
@AutoConfigureMockMvc
class Case002ApiBootReadinessIntegrationTest extends MySqlIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiShouldBeRunnableWithDefaultRuntimeBootstrapData() throws Exception {
        mockMvc.perform(get("/api/case-002/reservations/optimized")
                .param("agno", "10")
                .param("offset", "0")
                .param("limit", "20")
                .param("locale", "ko"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].rvno").exists());
    }
}
