package com.yowon.psc.case004.repro.domain;

public class Case004ProfileReport {

    private final String profile;
    private final long duplicateLeaseCount;
    private final long orphanLeaseCount;
    private final long idempotencyMismatchCount;
    private final double avgLatencyMs;
    private final double p95LatencyMs;
    private final long maxLatencyMs;
    private final long successCount;
    private final long failCount;

    public Case004ProfileReport(
        String profile,
        long duplicateLeaseCount,
        long orphanLeaseCount,
        long idempotencyMismatchCount,
        double avgLatencyMs,
        double p95LatencyMs,
        long maxLatencyMs,
        long successCount,
        long failCount
    ) {
        this.profile = profile;
        this.duplicateLeaseCount = duplicateLeaseCount;
        this.orphanLeaseCount = orphanLeaseCount;
        this.idempotencyMismatchCount = idempotencyMismatchCount;
        this.avgLatencyMs = avgLatencyMs;
        this.p95LatencyMs = p95LatencyMs;
        this.maxLatencyMs = maxLatencyMs;
        this.successCount = successCount;
        this.failCount = failCount;
    }

    public String getProfile() {
        return profile;
    }

    public long getDuplicateLeaseCount() {
        return duplicateLeaseCount;
    }

    public long getOrphanLeaseCount() {
        return orphanLeaseCount;
    }

    public long getIdempotencyMismatchCount() {
        return idempotencyMismatchCount;
    }

    public double getAvgLatencyMs() {
        return avgLatencyMs;
    }

    public double getP95LatencyMs() {
        return p95LatencyMs;
    }

    public long getMaxLatencyMs() {
        return maxLatencyMs;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getFailCount() {
        return failCount;
    }
}
