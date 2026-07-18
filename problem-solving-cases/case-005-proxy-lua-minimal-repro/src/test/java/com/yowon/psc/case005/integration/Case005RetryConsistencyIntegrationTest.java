package com.yowon.psc.case005.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.yowon.psc.case005.Case005ProxyLuaMinimalReproApplication;
import com.yowon.psc.case005.repro.infra.ProxyPoolSeedSupport;
import com.yowon.psc.case005.repro.v1.LegacyBorrowService;
import com.yowon.psc.case005.repro.v2.LuaBorrowService;
import com.yowon.psc.case005.support.RedisIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Case005ProxyLuaMinimalReproApplication.class, properties = {
    "app.case005.v2.lease-ttl-seconds=1"
})
class Case005RetryConsistencyIntegrationTest extends RedisIntegrationTestSupport {

    @Autowired
    private ProxyPoolSeedSupport seedSupport;

    @Autowired
    private LegacyBorrowService legacyBorrowService;

    @Autowired
    private LuaBorrowService luaBorrowService;

    @Test
    void v1CanLeaveAnOrphanLeaseAfterWriteThenFail() {
        seedSupport.resetAll(1);

        try {
            legacyBorrowService.borrow("app-a", "client-a", "10.0.0.1", "req-a", true);
        } catch (Exception ignored) {
        }

        String retried = legacyBorrowService.borrow("app-a", "client-a", "10.0.0.1", "req-a", false);

        assertThat(retried).isNull();
        assertThat(legacyBorrowService.activeLeaseCount()).isEqualTo(1);
    }

    @Test
    void v2ReusesTheSameResultWhenRetryingWithRequestId() {
        seedSupport.resetAll(1);

        try {
            luaBorrowService.borrow("app-a", "client-a", "10.0.0.1", "req-a", true);
        } catch (Exception ignored) {
        }

        String retried = luaBorrowService.borrow("app-a", "client-a", "10.0.0.1", "req-a", false);

        assertThat(retried).isEqualTo("proxy-1");
        assertThat(luaBorrowService.activeLeaseCount()).isEqualTo(1);
    }

    @Test
    void v2ShouldNotReplayARequestResultAfterLeaseHasExpired() throws Exception {
        seedSupport.resetAll(1);

        try {
            luaBorrowService.borrow("app-b", "client-b", "10.0.0.2", "req-b", true);
        } catch (Exception ignored) {
        }

        Thread.sleep(1200L);
        assertThat(luaBorrowService.activeLeaseCount()).isEqualTo(0);

        String retried = luaBorrowService.borrow("app-b", "client-b", "10.0.0.2", "req-b", false);

        assertThat(retried).isEqualTo("proxy-1");
        assertThat(luaBorrowService.activeLeaseCount()).isEqualTo(1);
    }
}
