package com.til.case006.client;

import org.springframework.http.HttpMethod;

import java.net.URL;

/**
 * 테스트용 BaseClient stub. 외부 HTTP 호출 없이 in-memory로 응답 생성.
 *
 * <p>운영 패턴 (ExternalDataCollector... extends BaseClient) 의 최소 재현.
 * 테스트에서 {@code new StubBaseClient(null)} 로 1000회 반복할 때 ThreadLocal 누수 메커니즘만 검증.
 */
public class StubBaseClient extends BaseClient<String, String> {

    private final String fakeResponse;

    public StubBaseClient(EndpointConfig config) {
        super(config);
        this.fakeResponse = "stub-response-" + System.nanoTime();
    }

    @Override
    protected String createClient(URL url, EndpointConfig.EndpointInfo info) {
        return "stub-client-" + url.getHost();
    }

    @Override
    protected <Data> String onRequest(URL url, HttpMethod method, Data data, String client) {
        return fakeResponse;
    }

    @Override
    protected void onFailed() {
        // no-op
    }
}
