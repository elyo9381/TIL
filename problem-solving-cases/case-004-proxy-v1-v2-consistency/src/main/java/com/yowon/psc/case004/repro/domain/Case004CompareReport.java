package com.yowon.psc.case004.repro.domain;

public class Case004CompareReport {

    private final String domain;
    private final int concurrency;
    private final int iterations;
    private final int poolSize;
    private final int appCount;
    private final int cooldownMinutes;
    private final Case004ProfileReport v1P3;
    private final Case004ProfileReport v1P4;
    private final Case004ProfileReport v2Target;

    public Case004CompareReport(
        String domain,
        int concurrency,
        int iterations,
        int poolSize,
        int appCount,
        int cooldownMinutes,
        Case004ProfileReport v1P3,
        Case004ProfileReport v1P4,
        Case004ProfileReport v2Target
    ) {
        this.domain = domain;
        this.concurrency = concurrency;
        this.iterations = iterations;
        this.poolSize = poolSize;
        this.appCount = appCount;
        this.cooldownMinutes = cooldownMinutes;
        this.v1P3 = v1P3;
        this.v1P4 = v1P4;
        this.v2Target = v2Target;
    }

    public String getDomain() {
        return domain;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public int getIterations() {
        return iterations;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public int getAppCount() {
        return appCount;
    }

    public int getCooldownMinutes() {
        return cooldownMinutes;
    }

    public Case004ProfileReport getV1P3() {
        return v1P3;
    }

    public Case004ProfileReport getV1P4() {
        return v1P4;
    }

    public Case004ProfileReport getV2Target() {
        return v2Target;
    }
}
