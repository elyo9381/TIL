package com.til.case006.controller;

import com.til.case006.client.EndpointConfig;
import com.til.case006.client.FixAClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.Map;

/**
 * Profile: fix-a-strategy
 *
 * <p>{@link FixAClient#requestWithStrategyA} 사용. finally에서 clearRequestState() 호출.
 *
 * <p>관찰 포인트: response 정상 반환되지만, 그 후 {@code getResponse()} 호출하면 null.
 * → 이 컨트롤러는 의도적으로 request 결과 + getResponse() 양쪽 결과를 응답에 포함해서
 *   A 전략의 무효화 버그를 보여줌.
 */
@Slf4j
@RestController
@RequestMapping("/api/case-006")
@RequiredArgsConstructor
public class FixAController {

    private final EndpointConfig endpointConfig;

    @GetMapping("/fix-a")
    public Map<String, Object> hit() throws Exception {
        FixAClient client = new FixAClient(endpointConfig);
        URL url = new URL("http://mock-target.local/data");

        String responseFromRequest = client.requestWithStrategyA(url, HttpMethod.GET, null);
        String responseFromGetter = client.getResponse();   // ← null !! (clearRequestState 됐으므로)

        return Map.of(
            "profile", "fix-a-strategy",
            "responseFromRequest", responseFromRequest,
            "responseFromGetter", String.valueOf(responseFromGetter),
            "warning", "getResponse() returns null after request - this is why A strategy was rejected"
        );
    }
}
