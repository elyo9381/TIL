package com.yowon.psc.common.metrics;

public class QueryMetricSnapshot {

    private final long sampleCount;
    private final long totalSqlCount;
    private final double averageLatencyMs;
    private final long p95LatencyMs;

    public QueryMetricSnapshot(long sampleCount, long totalSqlCount, double averageLatencyMs, long p95LatencyMs) {
        this.sampleCount = sampleCount;
        this.totalSqlCount = totalSqlCount;
        this.averageLatencyMs = averageLatencyMs;
        this.p95LatencyMs = p95LatencyMs;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public long getTotalSqlCount() {
        return totalSqlCount;
    }

    public double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public long getP95LatencyMs() {
        return p95LatencyMs;
    }
}
