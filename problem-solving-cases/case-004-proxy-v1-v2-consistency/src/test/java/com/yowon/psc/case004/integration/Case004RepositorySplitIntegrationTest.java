package com.yowon.psc.case004.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.yowon.psc.case004.support.RedisIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest(properties = {
    "app.seed.enabled=true",
    "app.seed.resource-count=12",
    "app.v1.artificial-race-delay-ms=8",
    "app.repro.default-domain=domain-a"
})
class Case004RepositorySplitIntegrationTest extends RedisIntegrationTestSupport {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextShouldExposeSplitRepositoriesInsteadOfSingleGodRepository() {
        assertThat(applicationContext.containsBean("case004LegacyRedisRepository")).isTrue();
        assertThat(applicationContext.containsBean("case004TargetRedisRepository")).isTrue();
        assertThat(applicationContext.containsBean("case004ScenarioRedisSupport")).isTrue();
        assertThat(applicationContext.containsBean("case004RedisRepository")).isFalse();
    }
}
