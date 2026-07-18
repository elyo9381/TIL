/**
 *    NOTE: Adaptation of an HTTP client base pattern. Author / copyright holder anonymized.
 *
 *
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
 */

package com.til.case006.client;

import com.til.case006.client.EndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.Validate;
import org.springframework.http.HttpMethod;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public abstract class BaseClient<RequestClient, Response> {
    private final ThreadLocal<RequestClient> requestClient = new ThreadLocal<>();
    private final ThreadLocal<Response> response = new ThreadLocal<>();
    private final ThreadLocal<Boolean> responseSuccess = ThreadLocal.withInitial(() -> false);

    public RequestClient getRequestClient() { return requestClient.get(); }
    public Response getResponse() { return response.get(); }
    public boolean isResponseSuccess() { return Boolean.TRUE.equals(responseSuccess.get()); }

    /**
     * request() 완료 후 ThreadLocal 정리.
     * 반드시 close() 또는 서브클래스의 종료 시점에서만 호출해야 한다.
     * request() 내부의 finally에 넣으면 getResponseBody() 등 다음 호출이 null을 받는다.
     * (RND-771: DefaultExchangeStrategies 1.15GB 누수 방지)
     */
    protected void clearRequestState() {
        this.requestClient.remove();
        this.response.remove();
        this.responseSuccess.remove();
    }


    private EndpointConfig endpointConfig;

    public BaseClient(EndpointConfig configuration) {
        this.endpointConfig = configuration;
    }


    public EndpointConfig getEndpointConfig() {
        return endpointConfig;
    }

    /**
     * Host 키로 info 가져오기
     * @param url
     * @return
     */
    protected EndpointConfig.EndpointInfo getEndpointInfo(URL url) {
        if(endpointConfig == null) return null;
        return endpointConfig.getEndpointInfo(url.getHost());
    }

    protected EndpointConfig.EndpointInfo getEndpointInfo(String url) {
        try {
            return getEndpointInfo(new URL(url));
        } catch (Exception e) {
            log.error("[BaseClient.getEndpointInfo] {}", e.getMessage(), e);
            return null;
        }
    }


    /**
     * 해당 Host의 마지막 접속시간 업데이트
     * @param url
     * @throws Exception
     */
    protected void setLastConnectDttm(URL url) throws Exception {
        // 현재시간으로 업데이트
        setLastConnectDttm(url, LocalDateTime.now());
    }

    protected void setLastConnectDttm(URL url, LocalDateTime newDttm) throws Exception {
        EndpointConfig.EndpointInfo info = getEndpointInfo(url);
        if(info != null) {
            log.debug("old LastDttm : {}", info.getLastConnectDttm());
            log.debug("new LastDttm : {}", newDttm);
            info.setLastConnectDttm(newDttm);
        }
    }


    /**
     * 건당 최대 대기시간 - (현재시간 - 마지막시간) 만큼 대기
     * @param url
     */
    protected synchronized void waitConnection(URL url) throws Exception {
        EndpointConfig.EndpointInfo info = getEndpointInfo(url);
        log.info("[waitConnection] url: {}, host: {}, info: {}", url, url.getHost(), info);
        if(info != null) {
            LocalDateTime currDttm = LocalDateTime.now();
            long diffFromLast = Duration.between(info.getLastConnectDttm(), currDttm).toMillis();
            long waitSec = info.getMaxWaitingSec() - diffFromLast;
            log.info("[waitConnection] maxWaitingSec: {}, diffFromLast: {}, waitSec: {}", info.getMaxWaitingSec(), diffFromLast, waitSec);

            if (waitSec > 0) {
                log.info("=================Wait {} mlisec================", waitSec);
                Thread.sleep(waitSec);
                log.info("=================Wait END================");
            }
        }
    }


    /**
     * String을 URL 오브젝트로 변환
     * @param url
     * @return
     */
    public URL stringToUrl(String url) {
        Validate.notEmpty(url, "Must supply a valid URL");
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed URL: " + url, e);
        }
    }

    protected <Data> Response request(String url, HttpMethod method, Data data) throws Exception {
        URL reqUrl = stringToUrl(url);
        // 미리 정의된 Client 사용
        return request(reqUrl, method, data);
    }
    public <Data> Response request(URL url, HttpMethod method, Data data) throws Exception {
        // 미리 정의된 Client 사용
        return this.request(url, method, data, createClient(url, getEndpointInfo(url)));
    }

    protected <Data> Response request(String url, HttpMethod method, Data data, RequestClient requestClient) throws Exception {
        URL reqUrl = stringToUrl(url);
        return this.request(reqUrl, method, data, requestClient);
    }
    public <Data> Response request(URL url, HttpMethod method, Data data, RequestClient requestClient) throws Exception {
        try {
            // request 건당 대기
            waitConnection(url);
            log.debug("request: [{}] {}", method, url);

            // request 수행
//            this.requestClient = createClient(url, getEndpointInfo(url));
            this.requestClient.set(requestClient);
            this.response.set(onRequest(url, method, data, requestClient));
            this.responseSuccess.set(true);

            // Update last connection time
            setLastConnectDttm(url);

            return this.response.get();

        } catch (Exception e) {
            log.error("reqest ERROR!! url: {} | msg: {}", url, e.getMessage(), e);

            this.requestClient.set(null);
            this.response.set(null);
            this.responseSuccess.set(false);
            onFailed();

            throw e;
        }
    }

    /**
     * EndpointInfo 기준으로 Client 생성
     * @param url
     * @return
     */
    protected abstract RequestClient createClient(URL url, EndpointConfig.EndpointInfo endpointInfo) throws Exception;

    /**
     * Client 기준으로 Request 수행 정의.
     *
     * @param url
     * @param method
     * @param reqData
     * @param client
     * @param <Data>
     * @return
     * @throws Exception
     */
    protected abstract <Data> Response onRequest(URL url, HttpMethod method, Data reqData, RequestClient client) throws Exception;

    protected abstract void onFailed();



    public <Data> Response requestGet(String url, Data body) throws Exception {
        return request(url, HttpMethod.GET, body);
    }

    public <Data> Response requestGet(URL url, Data body) throws Exception {
        return request(url, HttpMethod.GET, body);
    }

    public <Data> Response requestPost(String url, Data body) throws Exception {
        return request(url, HttpMethod.POST, body);
    }

    public <Data> Response requestPost(URL url, Data body) throws Exception {
        return request(url, HttpMethod.POST, body);
    }
}
