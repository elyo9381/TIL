/*
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    NOTE: This file is a learning-purpose adaptation of an HTTP client
 *    base class pattern. Original author / copyright holder anonymized.
 */
package com.til.case006.client;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * 외부 사이트 엔드포인트 별 설정. application.yml의 {@code app.http} 하위에 정의.
 *
 * <pre>
 * endpointMap:                    # 엔드포인트 host 별 설정값
 *   example.com:                  #   key: endpoint host (도메인)
 *     maxConnectionPerSec: 1      #   초당 최대 연결 건수
 *     useProxy: false             #   프록시 경유 여부
 *     defaultHeader:              #   요청 시 적용할 기본 헤더
 *       User-Agent: "..."
 *       Content-Type: "..."
 * </pre>
 *
 * <p>일반화된 패턴으로, 외부 사이트 다수를 동시에 다루는 데이터 수집형 시스템에서 흔히 등장.
 * BaseClient 가 host 별 throttling / header / proxy 결정을 외부화하기 위해 사용.
 */
@ConfigurationProperties(prefix = "app.http")
@Component
@Slf4j
public class EndpointConfig {

    @Value("${spring.application.name:case-006}")
    String appName;

    @Getter
    @Setter
    private Map<String, EndpointInfo> endpointMap = new HashMap<>();

    public String getAppName() {
        return appName;
    }

    public EndpointInfo getEndpointInfo(String hostKey) {
        try {
            log.debug("[EndpointConfig] getEndpointInfo - key: {}", hostKey);
            return endpointMap.get(hostKey);
        } catch (Exception e) {
            log.error("[EndpointConfig] getEndpointInfo error - key: {}, msg: {}", hostKey, e.getMessage(), e);
            return null;
        }
    }


    /**
     * 단일 엔드포인트의 throttling / 프록시 / 헤더 설정 + 마지막 접속 시각 trace.
     */
    @Data
    public static class EndpointInfo {
        protected long maxConnectionPerSec;        // 초당 최대 연결 건수
        protected boolean useProxy;                // 프록시 경유 여부
        protected boolean fixedProxy;              // 고정 프록시 사용 여부
        protected String fixedProxyIp = "";
        protected Map<String, String> defaultHeader;

        // non-configuration (런타임 trace)
        protected LocalDateTime lastConnectDttm;   // 마지막 host 접속 시각
        protected long maxWaitingSec;              // 건당 최대 대기시간 (ms)

        public EndpointInfo() {
            this.lastConnectDttm = LocalDateTime.now();
        }

        public void setMaxConnectionPerSec(long value) {
            this.maxConnectionPerSec = value;
            if (maxConnectionPerSec == 0L) {
                maxWaitingSec = 0;
            } else {
                maxWaitingSec = 1000L / maxConnectionPerSec;
            }
        }
    }
}
