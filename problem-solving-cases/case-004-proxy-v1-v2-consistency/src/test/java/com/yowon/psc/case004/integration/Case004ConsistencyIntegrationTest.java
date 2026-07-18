package com.yowon.psc.case004.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.yowon.psc.case004.repro.application.Case004ReproService;
import com.yowon.psc.case004.repro.domain.Case004CompareCommand;
import com.yowon.psc.case004.repro.domain.Case004Profile;
import com.yowon.psc.case004.repro.domain.Case004ProfileReport;
import com.yowon.psc.case004.support.RedisIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "app.seed.enabled=true",
    "app.seed.resource-count=12",
    "app.v1.artificial-race-delay-ms=8",
    "app.repro.default-domain=domain-a"
})
class Case004ConsistencyIntegrationTest extends RedisIntegrationTestSupport {

    @Autowired
    private Case004ReproService service;

    @Test
    void v1ShouldShowDuplicateBorrowRiskWhileV2ShouldNot() {
        Case004ProfileReport v1 = service.report(Case004Profile.V1_LEGACY, new Case004CompareCommand("domain-a", 240, 12));
        Case004ProfileReport v2 = service.report(Case004Profile.V2_TARGET, new Case004CompareCommand("domain-a", 240, 12));

        assertThat(v1.getDuplicateLeaseCount()).isGreaterThan(0);
        assertThat(v2.getDuplicateLeaseCount()).isEqualTo(0);
    }

    @Test
    void v1ShouldShowRetryConsistencyRiskWhileV2ShouldNot() {
        Case004ProfileReport v1 = service.report(Case004Profile.V1_LEGACY, new Case004CompareCommand("domain-a", 160, 10));
        Case004ProfileReport v2 = service.report(Case004Profile.V2_TARGET, new Case004CompareCommand("domain-a", 160, 10));

        assertThat(v1.getIdempotencyMismatchCount()).isGreaterThan(0);
        assertThat(v1.getOrphanLeaseCount()).isGreaterThan(0);
        assertThat(v2.getIdempotencyMismatchCount()).isEqualTo(0);
        assertThat(v2.getOrphanLeaseCount()).isEqualTo(0);
    }
}
