package com.yowon.psc.common.metrics;

public class SqlMeasuredExecution<T> {

    private final T result;
    private final long sqlCount;
    private final long latencyMs;

    public SqlMeasuredExecution(T result, long sqlCount, long latencyMs) {
        this.result = result;
        this.sqlCount = sqlCount;
        this.latencyMs = latencyMs;
    }

    public T getResult() {
        return result;
    }

    public long getSqlCount() {
        return sqlCount;
    }

    public long getLatencyMs() {
        return latencyMs;
    }
}
