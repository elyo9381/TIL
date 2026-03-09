package com.yowon.psc.case005.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.yowon.psc.case005.Case005ProxyLuaMinimalReproApplication;
import com.yowon.psc.case005.repro.infra.ProxyPoolSeedSupport;
import com.yowon.psc.case005.repro.v1.LegacyBorrowService;
import com.yowon.psc.case005.repro.v2.LuaBorrowService;
import com.yowon.psc.case005.support.RedisIntegrationTestSupport;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Case005ProxyLuaMinimalReproApplication.class, properties = {
    "app.case005.v1.artificial-race-delay-ms=20"
})
class Case005ConcurrentBorrowIntegrationTest extends RedisIntegrationTestSupport {

    @Autowired
    private ProxyPoolSeedSupport seedSupport;

    @Autowired
    private LegacyBorrowService legacyBorrowService;

    @Autowired
    private LuaBorrowService luaBorrowService;

    @Test
    void v1CanDuplicateAssignmentsUnderConcurrentBorrow() throws Exception {
        seedSupport.resetAll(1);
        long duplicateCount = runConcurrentBorrow(concurrency -> legacyBorrowService.borrow(
            "app-" + concurrency,
            "client-" + concurrency,
            "10.0.0." + concurrency,
            "req-" + concurrency
        ));

        assertThat(duplicateCount).isGreaterThan(0);
    }

    @Test
    void v2KeepsAssignmentsUniqueUnderConcurrentBorrow() throws Exception {
        seedSupport.resetAll(1);
        long duplicateCount = runConcurrentBorrow(concurrency -> luaBorrowService.borrow(
            "app-" + concurrency,
            "client-" + concurrency,
            "10.1.0." + concurrency,
            "req-" + concurrency
        ));

        assertThat(duplicateCount).isEqualTo(0);
    }

    private long runConcurrentBorrow(BorrowAction action) throws Exception {
        int concurrency = 40;
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch ready = new CountDownLatch(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(concurrency);
        ConcurrentLinkedQueue<String> assigned = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < concurrency; i++) {
            final int index = i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    String proxyId = action.borrow(index);
                    if (proxyId != null) {
                        assigned.add(proxyId);
                    }
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();
        executor.shutdownNow();

        Set<String> distinct = new HashSet<>(assigned);
        return assigned.size() - distinct.size();
    }

    @FunctionalInterface
    private interface BorrowAction {
        String borrow(int index);
    }
}
