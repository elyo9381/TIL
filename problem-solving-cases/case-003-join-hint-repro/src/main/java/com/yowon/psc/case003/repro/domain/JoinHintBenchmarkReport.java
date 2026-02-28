package com.yowon.psc.case003.repro.domain;

public class JoinHintBenchmarkReport {

    private final String serviceType;
    private final int limit;
    private final int iterations;
    private final LatencyStats withoutHint;
    private final LatencyStats withHint;

    public JoinHintBenchmarkReport(
        String serviceType,
        int limit,
        int iterations,
        LatencyStats withoutHint,
        LatencyStats withHint
    ) {
        this.serviceType = serviceType;
        this.limit = limit;
        this.iterations = iterations;
        this.withoutHint = withoutHint;
        this.withHint = withHint;
    }

    public String getServiceType() {
        return serviceType;
    }

    public int getLimit() {
        return limit;
    }

    public int getIterations() {
        return iterations;
    }

    public LatencyStats getWithoutHint() {
        return withoutHint;
    }

    public LatencyStats getWithHint() {
        return withHint;
    }

    public static class LatencyStats {

        private final double avgLatencyMs;
        private final long minLatencyMs;
        private final long maxLatencyMs;
        private final int sampleCount;

        public LatencyStats(double avgLatencyMs, long minLatencyMs, long maxLatencyMs, int sampleCount) {
            this.avgLatencyMs = avgLatencyMs;
            this.minLatencyMs = minLatencyMs;
            this.maxLatencyMs = maxLatencyMs;
            this.sampleCount = sampleCount;
        }

        public double getAvgLatencyMs() {
            return avgLatencyMs;
        }

        public long getMinLatencyMs() {
            return minLatencyMs;
        }

        public long getMaxLatencyMs() {
            return maxLatencyMs;
        }

        public int getSampleCount() {
            return sampleCount;
        }
    }
}
