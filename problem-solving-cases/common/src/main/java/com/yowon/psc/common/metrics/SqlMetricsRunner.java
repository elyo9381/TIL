package com.yowon.psc.common.metrics;

import java.util.function.Supplier;

public class SqlMetricsRunner {

    public <T> SqlMeasuredExecution<T> measure(Supplier<T> action) {
        SqlQueryCountHolder.reset();
        long startedAt = System.currentTimeMillis();
        try {
            T result = action.get();
            long elapsed = Math.max(1L, System.currentTimeMillis() - startedAt);
            return new SqlMeasuredExecution<>(result, SqlQueryCountHolder.getCount(), elapsed);
        } finally {
            SqlQueryCountHolder.clear();
        }
    }
}
