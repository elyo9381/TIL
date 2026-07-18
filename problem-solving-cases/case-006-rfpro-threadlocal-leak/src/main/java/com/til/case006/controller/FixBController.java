package com.til.case006.controller;

import com.til.case006.client.EndpointConfig;
import com.til.case006.client.FixBClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.Map;

/**
 * Profile: fix-b-strategy (운영 채택안)
 *
 * <p>try-with-resources 패턴으로 close() 자동 호출 → ThreadLocal 정리.
 * <p>request() 정상 흐름 유지 → getResponse() 정상 반환.
 */
@Slf4j
@RestController
@RequestMapping("/api/case-006")
@RequiredArgsConstructor
public class FixBController {

    private final EndpointConfig endpointConfig;

    @GetMapping("/fix-b")
    public Map<String, Object> hit() throws Exception {
        URL url = new URL("http://mock-target.local/data");

        try (FixBClient client = new FixBClient(endpointConfig)) {
            String responseFromRequest = client.request(url, HttpMethod.GET, null);
            String responseFromGetter = client.getResponse();   // ✅ 정상 (null 아님)

            return Map.of(
                "profile", "fix-b-strategy",
                "responseFromRequest", responseFromRequest,
                "responseFromGetter", responseFromGetter,
                "note", "try-with-resources auto-closes - ThreadLocal cleaned up after method exit"
            );
        }
    }
}
