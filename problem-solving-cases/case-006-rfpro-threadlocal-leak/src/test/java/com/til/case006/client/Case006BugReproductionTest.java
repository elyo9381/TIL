package com.til.case006.client;

import com.til.case006.inspector.ThreadLocalInspector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RND-771 누수 메커니즘 재현 테스트.
 *
 * <p>운영 코드 패턴: 매 요청마다 {@code new BugClient()} → close 없음 → ThreadLocal 누적.
 *
 * <p>이 테스트는 의도적으로 "delta가 충분히 크다"를 검증.
 * 회귀 방지 테스트(FixB)와는 정반대 의도. **bug가 진짜 누수를 만든다**는 사실 자체를 코드로 증명.
 *
 * <p>JVM 옵션: {@code --add-opens=java.base/java.lang=ALL-UNNAMED} (build.gradle.kts에 설정됨)
 */
class Case006BugReproductionTest {

    @Test
    @DisplayName("Bug 패턴(close 없는 BugClient)을 1000번 반복하면 ThreadLocal entry가 명확히 누적된다")
    void bugClient_shouldLeakThreadLocalEntries() throws Throwable {
        final int iterations = 1000;
        final AtomicInteger beforeCount = new AtomicInteger();
        final AtomicInteger afterCount = new AtomicInteger();
        final AtomicReference<Throwable> error = new AtomicReference<>();

        // 격리된 스레드에서 측정 (테스트 러너 ThreadLocal 노이즈 회피)
        Thread testThread = new Thread(() -> {
            try {
                // warmup → JDK 내부 lazy ThreadLocal 초기화
                BugClient warmup = new BugClient(null);
                warmup.request(new URL("http://test.local/warmup"), HttpMethod.GET, null,
                        "warmup-client");

                beforeCount.set(ThreadLocalInspector.countEntries());

                // ⚠️ 운영 패턴: 매번 new + close 없음
                for (int i = 0; i < iterations; i++) {
                    BugClient client = new BugClient(null);
                    client.request(new URL("http://test.local/" + i), HttpMethod.GET, null,
                            "req-" + i);
                    // close() 호출 안 함 → 누수
                }

                afterCount.set(ThreadLocalInspector.countEntries());
            } catch (Throwable t) {
                error.set(t);
            }
        }, "bug-repro-thread");

        testThread.start();
        testThread.join();
        if (error.get() != null) throw error.get();

        int delta = afterCount.get() - beforeCount.get();
        System.out.printf("[BugRepro] before=%d, after=%d, delta=%d (iterations=%d)%n",
                beforeCount.get(), afterCount.get(), delta, iterations);

        // 누수 신호: BugClient는 매 iteration마다 3개 ThreadLocal 객체 생성 → 최대 +3000
        // ThreadLocalMap 자동 정리(cleanSomeSlots) 일부 흡수해도 최소 수백 entry 누적
        assertThat(delta)
                .as("Bug 패턴은 명확한 누수를 만들어야 한다 (운영 RND-771 시나리오 재현)")
                .isGreaterThan(500);
    }
}
