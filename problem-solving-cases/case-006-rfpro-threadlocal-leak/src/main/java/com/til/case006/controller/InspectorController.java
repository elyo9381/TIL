package com.til.case006.controller;

import com.til.case006.inspector.ThreadLocalInspector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 진단용 컨트롤러. ThreadLocalMap entry 카운트 + 힙 사용량 노출.
 *
 * <p>부하 테스트 중 {@code /api/case-006/bug} 또는 {@code /api/case-006/fix-b} 를
 * 1000회 발사하면서 동시에 이 엔드포인트를 폴링하면 entry 누적 비교 가능.
 *
 * <pre>
 * # 부하 발사 (다른 터미널)
 * for i in {1..1000}; do curl -s localhost:8086/api/case-006/bug >/dev/null; done
 *
 * # 동시 폴링
 * while true; do curl -s localhost:8086/api/case-006/stats | jq; sleep 1; done
 * </pre>
 *
 * <p>주의: HTTP 요청은 여러 스레드(tomcat thread pool)에 분산되므로,
 * /stats 엔드포인트가 보는 thread와 /bug 엔드포인트가 사용한 thread가 다를 수 있음.
 * 정확한 entry 누적 확인은 단위 테스트(Case006*Test)가 더 신뢰성 높음.
 */
@Slf4j
@RestController
@RequestMapping("/api/case-006")
public class InspectorController {

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
        long maxMb = rt.maxMemory() / 1024 / 1024;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentThreadName", Thread.currentThread().getName());
        result.put("currentThreadEntries", ThreadLocalInspector.countEntries());
        result.put("heapUsedMB", usedMb);
        result.put("heapMaxMB", maxMb);
        result.put("heapUsedPercent", (usedMb * 100) / maxMb);
        return result;
    }
}
