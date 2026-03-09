package com.yowon.psc.case004.repro.application;

import com.yowon.psc.case004.repro.domain.Case004CompareCommand;
import com.yowon.psc.case004.repro.domain.Case004Profile;
import com.yowon.psc.case004.repro.domain.Case004ProfileReport;
import com.yowon.psc.case004.repro.infra.Case004LegacyRedisRepository;
import com.yowon.psc.case004.repro.infra.Case004ScenarioRedisSupport;
import com.yowon.psc.case004.repro.infra.Case004TargetRedisRepository;
import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Case004ReproService {

    private final Case004ScenarioRedisSupport scenarioSupport;
    private final Case004LegacyRedisRepository legacyRepository;
    private final Case004TargetRedisRepository targetRepository;
    private final int artificialRaceDelayMs;
    private final Object runLock = new Object();
    private final ThreadLocal<AtomicBoolean> runLockLostState = new ThreadLocal<>();

    public Case004ReproService(
        Case004ScenarioRedisSupport scenarioSupport,
        Case004LegacyRedisRepository legacyRepository,
        Case004TargetRedisRepository targetRepository,
        @Value("${app.v1.artificial-race-delay-ms:8}") int artificialRaceDelayMs
    ) {
        this.scenarioSupport = scenarioSupport;
        this.legacyRepository = legacyRepository;
        this.targetRepository = targetRepository;
        this.artificialRaceDelayMs = Math.max(0, artificialRaceDelayMs);
    }

    public Case004ProfileReport report(Case004Profile profile, Case004CompareCommand command) {
        return withRunLock(() -> runProfile(profile, normalize(command)));
    }

    private Case004ProfileReport runProfile(Case004Profile profile, Case004CompareCommand command) {
        scenarioSupport.resetScenario(profile, command.getDomain(), command.getPoolSize());
        long duplicateLeaseCount = runConcurrentBorrowScenario(profile, command);

        assertRunLockHealthy();
        long idempotencyMismatchCount = runIdempotencyScenario(profile, command);

        assertRunLockHealthy();
        long orphanLeaseCount = runOrphanScenario(profile, command);

        return new Case004ProfileReport(
            profile.getCode(),
            duplicateLeaseCount,
            orphanLeaseCount,
            idempotencyMismatchCount
        );
    }

    private long runConcurrentBorrowScenario(Case004Profile profile, Case004CompareCommand command) {
        assertRunLockHealthy();
        int concurrency = command.getConcurrency();
        ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, concurrency));
        CountDownLatch ready = new CountDownLatch(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(concurrency);

        ConcurrentLinkedQueue<String> successResources = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < concurrency; i++) {
            final int index = i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    String resourceId = borrow(
                        profile,
                        command.getDomain(),
                        "borrow-app",
                        "borrow-client-" + index,
                        "10.0.0." + (index % 240 + 1),
                        "borrow-request-" + index,
                        false
                    );
                    if (resourceId != null) {
                        successResources.add(resourceId);
                    }
                } catch (Exception ignored) {
                    // ignore for repro metric
                } finally {
                    done.countDown();
                }
            });
        }

        try {
            if (!ready.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out while waiting for workers to be ready");
            }
            start.countDown();
            if (!done.await(60, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out while waiting for workers to finish");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for concurrent scenario", e);
        } finally {
            executor.shutdownNow();
        }

        Set<String> distinct = new HashSet<>(successResources);
        return Math.max(0L, successResources.size() - distinct.size());
    }

    private long runIdempotencyScenario(Case004Profile profile, Case004CompareCommand command) {
        scenarioSupport.resetScenario(profile, command.getDomain(), command.getPoolSize());
        String requestId = "idem-request";

        String first = borrow(
            profile,
            command.getDomain(),
            "idem-app",
            "idem-client",
            "10.1.0.1",
            requestId,
            false
        );

        String second = borrow(
            profile,
            command.getDomain(),
            "idem-app",
            "idem-client",
            "10.1.0.1",
            requestId,
            false
        );

        return Objects.equals(first, second) ? 0L : 1L;
    }

    private long runOrphanScenario(Case004Profile profile, Case004CompareCommand command) {
        scenarioSupport.resetScenario(profile, command.getDomain(), command.getPoolSize());

        String appName = "orphan-app";
        String requestId = "orphan-request";

        try {
            borrow(
                profile,
                command.getDomain(),
                appName,
                "orphan-client",
                "10.2.0.1",
                requestId,
                true
            );
        } catch (Exception ignored) {
            // Simulated transport failure after write.
        }

        String retried = borrow(
            profile,
            command.getDomain(),
            appName,
            "orphan-client",
            "10.2.0.1",
            requestId,
            false
        );

        int activeCount = profile == Case004Profile.V2_TARGET
            ? targetRepository.countActiveLeases(command.getDomain(), appName)
            : legacyRepository.countActiveLeases(command.getDomain(), appName);

        int expected = retried == null ? 0 : 1;
        return Math.max(0, activeCount - expected);
    }

    private String borrow(
        Case004Profile profile,
        String domain,
        String appName,
        String clientName,
        String clientIp,
        String requestId,
        boolean failAfterWrite
    ) {
        switch (profile) {
            case V1_LEGACY:
                return legacyRepository.borrow(domain, appName, clientName, clientIp, requestId, failAfterWrite, artificialRaceDelayMs);
            case V2_TARGET:
                return targetRepository.borrow(domain, appName, clientName, clientIp, requestId, failAfterWrite);
            default:
                throw new IllegalArgumentException("Unsupported profile: " + profile);
        }
    }

    private Case004CompareCommand normalize(Case004CompareCommand command) {
        String domain = command.getDomain() == null || command.getDomain().isBlank()
            ? "domain-a"
            : command.getDomain().trim();
        int concurrency = Math.max(1, command.getConcurrency());
        int poolSize = Math.max(1, command.getPoolSize());
        return new Case004CompareCommand(domain, concurrency, poolSize);
    }

    private <T> T withRunLock(Supplier<T> action) {
        synchronized (runLock) {
            Duration lockTtl = Duration.ofMinutes(2);
            String lockToken = scenarioSupport.tryAcquireRunLock(lockTtl);
            if (lockToken == null) {
                throw new IllegalStateException("Another case-004 run is already in progress");
            }
            AtomicBoolean lockLost = new AtomicBoolean(false);
            runLockLostState.set(lockLost);
            ScheduledExecutorService lockKeepAlive = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "case004-lock-keepalive");
                t.setDaemon(true);
                return t;
            });

            lockKeepAlive.scheduleAtFixedRate(
                () -> {
                    try {
                        boolean refreshed = scenarioSupport.refreshRunLock(lockToken, lockTtl);
                        if (!refreshed) {
                            lockLost.set(true);
                        }
                    } catch (Exception ignored) {
                        lockLost.set(true);
                    }
                },
                30,
                30,
                TimeUnit.SECONDS
            );

            try {
                T result = action.get();
                if (lockLost.get()) {
                    throw new IllegalStateException("Run lock was lost during execution");
                }
                return result;
            } finally {
                lockKeepAlive.shutdownNow();
                runLockLostState.remove();
                scenarioSupport.releaseRunLock(lockToken);
            }
        }
    }

    private void assertRunLockHealthy() {
        AtomicBoolean lockLost = runLockLostState.get();
        if (lockLost != null && lockLost.get()) {
            throw new IllegalStateException("Run lock is no longer healthy");
        }
    }
}
