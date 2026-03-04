package com.yowon.psc.case004.repro.domain;

public class Case004CompareCommand {

    private final String domain;
    private final int concurrency;
    private final int iterations;
    private final int poolSize;
    private final int appCount;
    private final int cooldownMinutes;

    public Case004CompareCommand(
        String domain,
        int concurrency,
        int iterations,
        int poolSize,
        int appCount,
        int cooldownMinutes
    ) {
        this.domain = domain;
        this.concurrency = concurrency;
        this.iterations = iterations;
        this.poolSize = poolSize;
        this.appCount = appCount;
        this.cooldownMinutes = cooldownMinutes;
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
}
