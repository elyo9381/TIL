package com.yowon.psc.case004.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.yowon.psc.case004.repro.application.Case004ReproService;
import com.yowon.psc.case004.repro.domain.Case004CompareCommand;
import com.yowon.psc.case004.repro.domain.Case004CompareReport;
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
    void compareShouldShowV2RemovesDuplicateLeases() {
        Case004CompareReport report = service.compare(new Case004CompareCommand("domain-a", 240, 4, 12, 1, 0));

        assertThat(report.getV1P3().getDuplicateLeaseCount()).isGreaterThan(0);
        assertThat(report.getV1P4().getDuplicateLeaseCount()).isGreaterThan(0);
        assertThat(report.getV2Target().getDuplicateLeaseCount()).isEqualTo(0);
    }

    @Test
    void compareShouldShowV2FixesIdempotencyMismatch() {
        Case004CompareReport report = service.compare(new Case004CompareCommand("domain-a", 200, 3, 10, 1, 0));

        assertThat(report.getV1P3().getIdempotencyMismatchCount()).isGreaterThan(0);
        assertThat(report.getV1P4().getIdempotencyMismatchCount()).isGreaterThan(0);
        assertThat(report.getV2Target().getIdempotencyMismatchCount()).isEqualTo(0);
    }

    @Test
    void compareShouldShowV2PreventsOrphanLeasesAfterPartialFailure() {
        Case004CompareReport report = service.compare(new Case004CompareCommand("domain-a", 160, 3, 10, 1, 0));

        assertThat(report.getV1P3().getOrphanLeaseCount()).isGreaterThan(0);
        assertThat(report.getV1P4().getOrphanLeaseCount()).isGreaterThan(0);
        assertThat(report.getV2Target().getOrphanLeaseCount()).isEqualTo(0);
    }
}
