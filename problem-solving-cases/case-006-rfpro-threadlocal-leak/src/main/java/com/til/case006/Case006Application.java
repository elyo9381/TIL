package com.til.case006;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * case-006: ThreadLocal Leak Reproduction Lab.
 *
 * <p>실행 방법:
 * <pre>
 * ./gradlew :case-006-rfpro-threadlocal-leak:bootRun
 * </pre>
 *
 * <p>제공 엔드포인트:
 * <ul>
 *   <li>{@code GET /api/case-006/bug} - RND-771 직전 상태 (close 없음)</li>
 *   <li>{@code GET /api/case-006/fix-a} - A 전략 (request finally clear, getResponseBody 무효화)</li>
 *   <li>{@code GET /api/case-006/fix-b} - B 전략 (close에서만 clear, 운영 채택안)</li>
 *   <li>{@code GET /api/case-006/stats} - 현재 스레드 ThreadLocal entry 카운트 + 힙 사용량</li>
 * </ul>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class Case006Application {
    public static void main(String[] args) {
        SpringApplication.run(Case006Application.class, args);
    }
}
