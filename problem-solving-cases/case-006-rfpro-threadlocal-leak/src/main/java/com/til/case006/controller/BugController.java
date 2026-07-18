package com.til.case006.controller;

import com.til.case006.client.BugClient;
import com.til.case006.client.EndpointConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.Map;

/**
 * Profile: bug-set-null
 *
 * <p>매 요청마다 {@code new BugClient()} → ThreadLocal 객체 3개 생성 →
 * close()/clearRequestState() 호출 없이 메서드 종료 → ThreadLocalMap entry 누적.
 *
 * <p>이게 RND-771 운영 패턴 (ExternalDataCollector 계열) 의 정확한 재현.
 */
@Slf4j
@RestController
@RequestMapping("/api/case-006")
@RequiredArgsConstructor
public class BugController {

    private final EndpointConfig endpointConfig;

    @GetMapping("/bug")
    public Map<String, Object> hit() throws Exception {
        // ⚠️ 매 요청마다 new — RND-771 운영 패턴
        BugClient client = new BugClient(endpointConfig);
        URL url = new URL("http://mock-target.local/data");
        String response = client.request(url, HttpMethod.GET, null);

        // close() 안 부름 → ThreadLocal 그대로 남음
        return Map.of(
            "profile", "bug-set-null",
            "response", response,
            "warning", "No clearRequestState() / close() called - this is the leak pattern"
        );
    }
}
