package com.yowon.psc.common.metrics;

public final class SqlQueryCountHolder {

    private static final ThreadLocal<Long> QUERY_COUNT = ThreadLocal.withInitial(() -> 0L);

    private SqlQueryCountHolder() {
    }

    public static void reset() {
        QUERY_COUNT.set(0L);
    }

    public static void addCount(long count) {
        if (count <= 0L) {
            return;
        }
        QUERY_COUNT.set(QUERY_COUNT.get() + count);
    }

    public static long getCount() {
        return QUERY_COUNT.get();
    }

    public static void clear() {
        QUERY_COUNT.remove();
    }
}
