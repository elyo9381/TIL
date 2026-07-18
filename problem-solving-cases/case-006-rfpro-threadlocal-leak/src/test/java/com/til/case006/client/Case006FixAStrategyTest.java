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
 * A 전략 검증 — 누수는 막지만 getResponseBody() null 무효화 재현.
 *
 * <p>이 테스트의 두 단언:
 * <ol>
 *   <li>1000회 반복해도 ThreadLocal entry 누수 없음 (delta &lt; 50)</li>
 *   <li>request 후 getResponse() 호출하면 null 반환 (운영에서 거부된 이유)</li>
 * </ol>
 */
class Case006FixAStrategyTest {

    @Test
    @DisplayName("FixA: 1000번 반복해도 ThreadLocal 누수가 없어야 한다")
    void fixAClient_shouldNotLeak_butGetResponseReturnsNull() throws Throwable {
        final int iterations = 1000;
        final AtomicInteger beforeCount = new AtomicInteger();
        final AtomicInteger afterCount = new AtomicInteger();
        final AtomicReference<Throwable> error = new AtomicReference<>();

        Thread testThread = new Thread(() -> {
            try {
                FixAClient warmup = new FixAClient(null);
                warmup.requestWithStrategyA(new URL("http://test.local/warmup"), HttpMethod.GET, null);

                beforeCount.set(ThreadLocalInspector.countEntries());

                for (int i = 0; i < iterations; i++) {
                    FixAClient client = new FixAClient(null);
                    client.requestWithStrategyA(new URL("http://test.local/" + i), HttpMethod.GET, null);
                }

                afterCount.set(ThreadLocalInspector.countEntries());
            } catch (Throwable t) {
                error.set(t);
            }
        }, "fix-a-test-thread");

        testThread.start();
        testThread.join();
        if (error.get() != null) throw error.get();

        int delta = afterCount.get() - beforeCount.get();
        System.out.printf("[FixA] before=%d, after=%d, delta=%d%n",
                beforeCount.get(), afterCount.get(), delta);

        assertThat(delta)
                .as("FixA: finally clearRequestState 로 누수는 0")
                .isLessThan(50);
    }

    @Test
    @DisplayName("FixA: requestWithStrategyA() 후 getResponse()는 null을 반환한다 (운영 거부 이유)")
    void fixAClient_getResponseAfterRequest_returnsNull() throws Exception {
        FixAClient client = new FixAClient(null);

        String responseFromRequest = client.requestWithStrategyA(
                new URL("http://test.local/data"), HttpMethod.GET, null);
        String responseFromGetter = client.getResponse();

        assertThat(responseFromRequest)
                .as("request의 직접 반환값은 정상")
                .isNotNull();

        // ⚠️ A 전략의 치명적 문제 — 운영에서 이 패턴이 거부된 이유
        assertThat(responseFromGetter)
                .as("FixA: getResponse()는 null - finally가 ThreadLocal을 비웠음")
                .isNull();
    }
}
