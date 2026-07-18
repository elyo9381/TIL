package com.til.case006.inspector;

import java.lang.reflect.Field;

/**
 * 현재 스레드의 ThreadLocalMap Entry 개수를 리플렉션으로 카운트하는 테스트 유틸.
 *
 * <p>JDK 내부의 {@code Thread.threadLocals}, {@code ThreadLocal$ThreadLocalMap.table}에
 * 접근하므로 실행 시 다음 JVM 옵션이 필요:
 * <pre>
 *   --add-opens=java.base/java.lang=ALL-UNNAMED
 * </pre>
 *
 * <p>메모리 누수 검증 용도로만 사용 — 프로덕션 코드 금지.
 */
public final class ThreadLocalInspector {

    private ThreadLocalInspector() {}

    /**
     * 주어진 스레드의 ThreadLocalMap에 현재 등록된 Entry(non-null) 개수를 반환.
     * stale(value=null, key=null) entry도 포함하므로 누수 감지에 적합.
     */
    public static int countEntries(Thread thread) {
        try {
            Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);
            Object threadLocalMap = threadLocalsField.get(thread);
            if (threadLocalMap == null) return 0;

            Field tableField = threadLocalMap.getClass().getDeclaredField("table");
            tableField.setAccessible(true);
            Object[] table = (Object[]) tableField.get(threadLocalMap);
            if (table == null) return 0;

            int count = 0;
            for (Object entry : table) {
                if (entry != null) count++;
            }
            return count;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "ThreadLocalMap 접근 실패. --add-opens=java.base/java.lang=ALL-UNNAMED 필요", e);
        }
    }

    /** 현재 스레드의 ThreadLocalMap Entry 개수 */
    public static int countEntries() {
        return countEntries(Thread.currentThread());
    }
}
