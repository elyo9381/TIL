package com.til.case006.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

import java.net.URL;

/**
 * Profile: fix-a-strategy
 *
 * <p>A 전략 — {@code request()} 의 finally 블록에서 {@code clearRequestState()} 호출.
 *
 * <p>장점: ThreadLocal 누수 즉시 해결.
 * <p>치명 단점: {@code request()} 가 return 후 호출 측이 {@code getResponse()},
 * {@code getResponseBody()} 등을 호출하면 <b>null 반환</b>.
 * 운영 코드의 광범위한 의존 패턴을 깨뜨려서 거부된 전략.
 *
 * <pre>{@code
 * FixAClient client = new FixAClient(config);
 * client.request(url, GET, data);
 * String body = client.getResponse();   // ← null !!
 * }</pre>
 *
 * <p>이 클라이언트의 검증 포인트:
 * <ul>
 *   <li>1000회 반복 → ThreadLocal entry 누적 0 (성공)</li>
 *   <li>request() 후 getResponse() → null (이게 무효화 버그 재현)</li>
 * </ul>
 *
 * <p>주의: 이 클래스는 {@link BaseClient#request} 의 흐름을 그대로 사용하지 않고,
 * finally 안에 {@code clearRequestState()} 를 끼워넣은 변형 흐름을 직접 구현한다.
 * BaseClient.request() 자체에는 finally 가 없기 때문 (정상 운영 형태).
 */
@Slf4j
public class FixAClient extends BaseClient<String, String> {

    private final String fakeResponse;

    public FixAClient(EndpointConfig endpointConfig) {
        super(endpointConfig);
        this.fakeResponse = "stub-response-" + System.nanoTime();
    }

    /**
     * A 전략을 적용한 request — 부모 BaseClient.request() 를 그대로 쓰지 않고,
     * finally에 clearRequestState() 추가한 형태.
     */
    public <Data> String requestWithStrategyA(URL url, HttpMethod method, Data data) throws Exception {
        try {
            return super.request(url, method, data, createClient(url, null));
        } finally {
            // ⚠️ A 전략의 핵심 — finally 에서 clear
            // 누수는 막지만, 호출 측이 이후 getResponse() 호출하면 null 받음
            clearRequestState();
        }
    }

    @Override
    protected String createClient(URL url, EndpointConfig.EndpointInfo endpointInfo) {
        return "client-for-" + url.getHost();
    }

    @Override
    protected <Data> String onRequest(URL url, HttpMethod method, Data reqData, String client) {
        log.debug("[FixAClient] onRequest url={} client={}", url, client);
        return fakeResponse;
    }

    @Override
    protected void onFailed() {
        log.warn("[FixAClient] onFailed");
    }
}
