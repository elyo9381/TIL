package com.yowon.psc.case004.repro.seed;

import com.yowon.psc.case004.repro.domain.Case004Profile;
import com.yowon.psc.case004.repro.infra.Case004ScenarioRedisSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Case004RedisSeedInitializer implements CommandLineRunner {

    private final Case004ScenarioRedisSupport scenarioSupport;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.resource-count:12}")
    private int resourceCount;

    @Value("${app.repro.default-domain:domain-a}")
    private String domain;

    public Case004RedisSeedInitializer(Case004ScenarioRedisSupport scenarioSupport) {
        this.scenarioSupport = scenarioSupport;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        scenarioSupport.resetScenario(Case004Profile.V1_LEGACY, domain, Math.max(1, resourceCount));
    }
}
