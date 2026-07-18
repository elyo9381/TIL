package com.yowon.psc.case004.repro.domain;

public class Case004CompareCommand {

    private final String domain;
    private final int concurrency;
    private final int poolSize;

    public Case004CompareCommand(String domain, int concurrency, int poolSize) {
        this.domain = domain;
        this.concurrency = concurrency;
        this.poolSize = poolSize;
    }

    public String getDomain() {
        return domain;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public int getPoolSize() {
        return poolSize;
    }
}
