package com.til.case006.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.net.URL;

/**
 * Profile: fix-b-strategy (운영 채택안, commit 29cfed0)
 *
 * <p>B 전략 — {@link AutoCloseable#close()} 에서만 {@code clearRequestState()} 호출.
 * 호출 측은 try-with-resources 강제.
 *
 * <p>장점:
 * <ul>
 *   <li>{@code request()} 정상 흐름 유지 → {@code getResponse()} 정상 동작</li>
 *   <li>호출 측에 try-with-resources 만 추가하면 끝 (점진 마이그레이션)</li>
 *   <li>BaseClient 시그니처 변경 0</li>
 * </ul>
 *
 * <p>이 클라이언트의 검증 포인트:
 * <ul>
 *   <li>try-with-resources 사용 시 close() 자동 호출 → ThreadLocal entry 누적 0</li>
 *   <li>close() 호출 전이라면 getResponse() 정상 반환 (null 아님)</li>
 *   <li>1000회 반복 + try-with-resources → entry delta &lt; 50</li>
 * </ul>
 *
 * <pre>{@code
 * try (FixBClient client = new FixBClient(config)) {
 *     client.request(url, GET, data);
 *     String body = client.getResponse();   // ✅ 정상 (null 아님)
 * }
 * // try 종료 → close() → clearRequestState() → ThreadLocal remove
 * }</pre>
 */
@Slf4j
public class FixBClient extends BaseClient<String, String> implements AutoCloseable {

    private final String fakeResponse;

    public FixBClient(EndpointConfig endpointConfig) {
        super(endpointConfig);
        this.fakeResponse = "stub-response-" + System.nanoTime();
    }

    @Override
    public void close() {
        log.debug("[FixBClient] close - clearRequestState 호출");
        super.clearRequestState();
    }

    @Override
    protected String createClient(URL url, EndpointConfig.EndpointInfo endpointInfo) {
        return "client-for-" + url.getHost();
    }

    @Override
    protected <Data> String onRequest(URL url, HttpMethod method, Data reqData, String client) {
        log.debug("[FixBClient] onRequest url={} client={}", url, client);
        return fakeResponse;
    }

    @Override
    protected void onFailed() {
        log.warn("[FixBClient] onFailed");
    }
}
