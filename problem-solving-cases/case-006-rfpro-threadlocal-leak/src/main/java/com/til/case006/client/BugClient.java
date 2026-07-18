package com.til.case006.client;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;

/**
 * Profile: bug-set-null
 *
 * <p>운영 RND-771 직전 상태 재현. catch 블록의 {@code set(null)}만 있고
 * {@code clearRequestState()} / {@code close()} 호출 자체가 없는 클라이언트.
 *
 * <p>이 클라이언트로 1000회 반복하면:
 * <ul>
 *   <li>각 요청마다 새 ThreadLocal 객체 3개 생성 (instance 필드 초기화 시)</li>
 *   <li>각 ThreadLocal 객체가 호출 스레드의 ThreadLocalMap에 entry 박힘</li>
 *   <li>cleanup 없으므로 entry 누적 → 최대 +3000 entry</li>
 * </ul>
 *
 * @see com.til.case006.client.FixAClient
 * @see com.til.case006.client.FixBClient
 */
@Slf4j
public class BugClient extends BaseClient<String, String> {

    private final String fakeResponse;

    public BugClient(EndpointConfig endpointConfig) {
        super(endpointConfig);
        this.fakeResponse = "stub-response-" + System.nanoTime();
    }

    @Override
    protected String createClient(URL url, EndpointConfig.EndpointInfo endpointInfo) {
        return "client-for-" + url.getHost();
    }

    @Override
    protected <Data> String onRequest(URL url, org.springframework.http.HttpMethod method,
                                       Data reqData, String client) {
        log.debug("[BugClient] onRequest url={} client={}", url, client);
        return fakeResponse;
    }

    @Override
    protected void onFailed() {
        log.warn("[BugClient] onFailed");
    }

    /*
     * ⚠️ 의도적으로 close() / clearRequestState() 호출 없음.
     *    RND-771 직전 운영 상태 재현용.
     */
}
