package com.yowon.psc.case004.repro.application;

import com.yowon.psc.case004.repro.domain.Case004CompareCommand;
import com.yowon.psc.case004.repro.domain.Case004CompareReport;
import com.yowon.psc.case004.repro.domain.Case004Profile;
import com.yowon.psc.case004.repro.domain.Case004ProfileReport;
import com.yowon.psc.case004.repro.infra.Case004RedisRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Case004ReproService {

    private final Case004RedisRepository repository;
    private final int artificialRaceDelayMs;
    private final Object runLock = new Object();
    private final ThreadLocal<AtomicBoolean> runLockLostState = new ThreadLocal<>();

    public Case004ReproService(
        Case004RedisRepository repository,
        @Value("${app.v1.artificial-race-delay-ms:8}") int artificialRaceDelayMs
    ) {
        this.repository = repository;
        this.artificialRaceDelayMs = Math.max(0, artificialRaceDelayMs);
    }

    public Case004CompareReport compare(Case004CompareCommand command) {
        return withRunLock(() -> {
            Case004CompareCommand normalized = normalize(command);

            Case004ProfileReport v1P3 = runProfile(Case004Profile.V1_P3, normalized);
            Case004ProfileReport v1P4 = runProfile(Case004Profile.V1_P4, normalized);
            Case004ProfileReport v2Target = runProfile(Case004Profile.V2_TARGET, normalized);

            return new Case004CompareReport(
                normalized.getDomain(),
                normalized.getConcurrency(),
                normalized.getIterations(),
                normalized.getPoolSize(),
                normalized.getAppCount(),
                normalized.getCooldownMinutes(),
                v1P3,
                v1P4,
                v2Target
            );
        });
    }

    public Case004ProfileReport report(Case004Profile profile, Case004CompareCommand command) {
        return withRunLock(() -> runProfile(profile, normalize(command)));
    }

    private Case004ProfileReport runProfile(Case004Profile profile, Case004CompareCommand command) {
        long duplicateLeaseCount = 0L;
        long successCount = 0L;
        long failCount = 0L;
        List<Long> latencies = new ArrayList<>();

        for (int iteration = 0; iteration < command.getIterations(); iteration++) {
            assertRunLockHealthy();
            repository.resetScenario(profile, command.getDomain(), command.getPoolSize());
            ScenarioMetrics metrics = runConcurrentBorrowScenario(profile, command, iteration);
            duplicateLeaseCount += metrics.duplicateLeaseCount;
            successCount += metrics.successCount;
            failCount += metrics.failCount;
            latencies.addAll(metrics.latencyMs);
        }

        assertRunLockHealthy();
        long idempotencyMismatchCount = runIdempotencyScenario(profile, command);
        assertRunLockHealthy();
        long orphanLeaseCount = runOrphanScenario(profile, command);

        return new Case004ProfileReport(
            profile.getCode(),
            duplicateLeaseCount,
            orphanLeaseCount,
            idempotencyMismatchCount,
            average(latencies),
            percentile(latencies, 95),
            max(latencies),
            successCount,
            failCount
        );
    }

    private ScenarioMetrics runConcurrentBorrowScenario(
        Case004Profile profile,
        Case004CompareCommand command,
        int iteration
    ) {
        assertRunLockHealthy();
        int concurrency = command.getConcurrency();
        // Use one worker per request so all tasks can rendezvous on the same start latch.
        int threadCount = Math.max(1, concurrency);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(concurrency);

        ConcurrentLinkedQueue<String> successResources = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Integer> failures = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < concurrency; i++) {
            final int index = i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    String appName = "app-" + (index % Math.max(1, command.getAppCount()));
                    String clientName = "pod-" + index;
                    String clientIp = "10.0.0." + (index % 240 + 1);
                    String requestId = "iter-" + iteration + "-req-" + index;

                    long startedAt = System.currentTimeMillis();
                    String resourceId = borrow(
                        profile,
                        command.getDomain(),
                        appName,
                        clientName,
                        clientIp,
                        requestId,
                        command.getCooldownMinutes(),
                        false
                    );
                    long latency = Math.max(1L, System.currentTimeMillis() - startedAt);
                    latencies.add(latency);

                    if (resourceId != null) {
                        successResources.add(resourceId);
                    } else {
                        failures.add(1);
                    }
                } catch (Exception e) {
                    failures.add(1);
                } finally {
                    done.countDown();
                }
            });
        }

        try {
            boolean allReady = ready.await(30, TimeUnit.SECONDS);
            if (!allReady) {
                throw new IllegalStateException("Timed out while waiting for workers to be ready");
            }
            start.countDown();
            boolean allDone = done.await(60, TimeUnit.SECONDS);
            if (!allDone) {
                throw new IllegalStateException("Timed out while waiting for workers to finish");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for concurrent scenario", e);
        } finally {
            executor.shutdownNow();
        }

        assertRunLockHealthy();

        Set<String> distinct = new HashSet<>(successResources);
        long duplicate = Math.max(0L, successResources.size() - distinct.size());

        return new ScenarioMetrics(
            duplicate,
            successResources.size(),
            failures.size(),
            new ArrayList<>(latencies)
        );
    }

    private long runIdempotencyScenario(Case004Profile profile, Case004CompareCommand command) {
        repository.resetScenario(profile, command.getDomain(), command.getPoolSize());
        String requestId = "idem-request";

        String first = borrow(
            profile,
            command.getDomain(),
            "idem-app",
            "idem-client",
            "10.1.0.1",
            requestId,
            command.getCooldownMinutes(),
            false
        );

        String second = borrow(
            profile,
            command.getDomain(),
            "idem-app",
            "idem-client",
            "10.1.0.1",
            requestId,
            command.getCooldownMinutes(),
            false
        );

        return Objects.equals(first, second) ? 0L : 1L;
    }

    private long runOrphanScenario(Case004Profile profile, Case004CompareCommand command) {
        repository.resetScenario(profile, command.getDomain(), command.getPoolSize());

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
                command.getCooldownMinutes(),
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
            command.getCooldownMinutes(),
            false
        );

        int activeCount;
        if (profile == Case004Profile.V2_TARGET) {
            activeCount = repository.countActiveV2Leases(command.getDomain(), appName);
        } else {
            activeCount = repository.countActiveV1Leases(command.getDomain(), appName);
        }

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
        int cooldownMinutes,
        boolean failAfterWrite
    ) {
        switch (profile) {
            case V1_P3:
                return repository.borrowV1(domain, appName, clientName, clientIp, requestId, cooldownMinutes, true, failAfterWrite, artificialRaceDelayMs);
            case V1_P4:
                return repository.borrowV1(domain, appName, clientName, clientIp, requestId, cooldownMinutes, false, failAfterWrite, artificialRaceDelayMs);
            case V2_TARGET:
                return repository.borrowV2(domain, appName, clientName, clientIp, requestId, failAfterWrite);
            default:
                throw new IllegalArgumentException("Unsupported profile: " + profile);
        }
    }

    private Case004CompareCommand normalize(Case004CompareCommand command) {
        String domain = command.getDomain() == null || command.getDomain().isBlank()
            ? "domain-a"
            : command.getDomain().trim();
        int concurrency = Math.max(1, command.getConcurrency());
        int iterations = Math.max(1, command.getIterations());
        int poolSize = Math.max(1, command.getPoolSize());
        int appCount = Math.max(1, command.getAppCount());
        int cooldown = Math.max(0, command.getCooldownMinutes());
        return new Case004CompareCommand(domain, concurrency, iterations, poolSize, appCount, cooldown);
    }

    private <T> T withRunLock(Supplier<T> action) {
        synchronized (runLock) {
            Duration lockTtl = Duration.ofMinutes(2);
            String lockToken = repository.tryAcquireRunLock(lockTtl);
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
                        boolean refreshed = repository.refreshRunLock(lockToken, lockTtl);
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
                repository.releaseRunLock(lockToken);
            }
        }
    }

    private void assertRunLockHealthy() {
        AtomicBoolean lockLost = runLockLostState.get();
        if (lockLost != null && lockLost.get()) {
            throw new IllegalStateException("Run lock is no longer healthy");
        }
    }

    private static double average(List<Long> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        long total = 0L;
        for (Long value : values) {
            total += value;
        }
        return ((double) total) / values.size();
    }

    private static double percentile(List<Long> values, int p) {
        if (values.isEmpty()) {
            return 0.0;
        }
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil((p / 100.0) * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(index);
    }

    private static long max(List<Long> values) {
        long max = 0L;
        for (Long value : values) {
            max = Math.max(max, value);
        }
        return max;
    }

    private static final class ScenarioMetrics {
        private final long duplicateLeaseCount;
        private final long successCount;
        private final long failCount;
        private final List<Long> latencyMs;

        private ScenarioMetrics(long duplicateLeaseCount, long successCount, long failCount, List<Long> latencyMs) {
            this.duplicateLeaseCount = duplicateLeaseCount;
            this.successCount = successCount;
            this.failCount = failCount;
            this.latencyMs = latencyMs;
        }
    }
}
