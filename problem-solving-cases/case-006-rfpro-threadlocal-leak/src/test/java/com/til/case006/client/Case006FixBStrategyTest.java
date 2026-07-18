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
 * B 전략 검증 — 운영 채택안 (commit 29cfed0).
 *
 * <p>핵심 단언:
 * <ol>
 *   <li>1000회 반복 + try-with-resources 로 ThreadLocal 누수 없음</li>
 *   <li>request 후 getResponse() 정상 반환 (close 전이므로)</li>
 *   <li>close 후에는 ThreadLocal cleanup 됨</li>
 * </ol>
 */
class Case006FixBStrategyTest {

    @Test
    @DisplayName("FixB: 1000번 반복 + try-with-resources 패턴에서 ThreadLocal 누수가 없다")
    void fixBClient_withTryWithResources_doesNotLeak() throws Throwable {
        final int iterations = 1000;
        final AtomicInteger beforeCount = new AtomicInteger();
        final AtomicInteger afterCount = new AtomicInteger();
        final AtomicReference<Throwable> error = new AtomicReference<>();

        Thread testThread = new Thread(() -> {
            try {
                try (FixBClient warmup = new FixBClient(null)) {
                    warmup.request(new URL("http://test.local/warmup"), HttpMethod.GET, null,
                            "warmup-client");
                }

                beforeCount.set(ThreadLocalInspector.countEntries());

                for (int i = 0; i < iterations; i++) {
                    try (FixBClient client = new FixBClient(null)) {
                        client.request(new URL("http://test.local/" + i), HttpMethod.GET, null,
                                "req-" + i);
                    }
                }

                afterCount.set(ThreadLocalInspector.countEntries());
            } catch (Throwable t) {
                error.set(t);
            }
        }, "fix-b-test-thread");

        testThread.start();
        testThread.join();
        if (error.get() != null) throw error.get();

        int delta = afterCount.get() - beforeCount.get();
        System.out.printf("[FixB] before=%d, after=%d, delta=%d%n",
                beforeCount.get(), afterCount.get(), delta);

        assertThat(delta)
                .as("FixB: close()의 clearRequestState 로 누수 없음 (운영 RND-771 수정안)")
                .isLessThan(50);
    }

    @Test
    @DisplayName("FixB: try 블록 안에서는 getResponse()가 정상 반환된다 (A와의 결정적 차이)")
    void fixBClient_getResponse_returnsValueBeforeClose() throws Exception {
        try (FixBClient client = new FixBClient(null)) {
            String responseFromRequest = client.request(
                    new URL("http://test.local/data"), HttpMethod.GET, null, "test-client");
            String responseFromGetter = client.getResponse();

            assertThat(responseFromRequest).isNotNull();
            assertThat(responseFromGetter)
                    .as("FixB: close 전이므로 getResponse() 정상 반환 (A 전략과 결정적 차이)")
                    .isNotNull()
                    .startsWith("stub-response");
        }
        // try 종료 → close() → clearRequestState() → ThreadLocal 정리됨
    }

    @Test
    @DisplayName("FixB: close() 명시 호출 후 ThreadLocal 값들은 모두 정리된다")
    void fixBClient_afterClose_threadLocalsAreCleared() throws Exception {
        FixBClient client = new FixBClient(null);
        client.request(new URL("http://test.local/data"), HttpMethod.GET, null, "test-client");
        assertThat(client.getResponse()).isNotNull();   // close 전 정상

        client.close();   // 명시적 close

        assertThat(client.getResponse()).as("close 후 response ThreadLocal 정리").isNull();
        assertThat(client.getRequestClient()).as("close 후 requestClient 정리").isNull();
        assertThat(client.isResponseSuccess()).as("close 후 responseSuccess 정리(false)").isFalse();
    }
}
