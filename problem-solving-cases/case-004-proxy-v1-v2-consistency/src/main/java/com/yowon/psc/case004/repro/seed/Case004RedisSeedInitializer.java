package com.yowon.psc.case004.repro.seed;

import com.yowon.psc.case004.repro.domain.Case004Profile;
import com.yowon.psc.case004.repro.infra.Case004RedisRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Case004RedisSeedInitializer implements CommandLineRunner {

    private final Case004RedisRepository repository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.resource-count:12}")
    private int resourceCount;

    @Value("${app.repro.default-domain:domain-a}")
    private String domain;

    public Case004RedisSeedInitializer(Case004RedisRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        repository.resetScenario(Case004Profile.V1_P4, domain, Math.max(1, resourceCount));
    }
}
