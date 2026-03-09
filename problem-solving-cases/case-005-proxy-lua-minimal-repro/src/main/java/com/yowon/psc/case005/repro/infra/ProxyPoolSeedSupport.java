package com.yowon.psc.case005.repro.infra;

import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProxyPoolSeedSupport {

    private static final String PREFIX = "repro:case005:";

    private final StringRedisTemplate redisTemplate;

    public ProxyPoolSeedSupport(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void resetAll(int poolSize) {
        clearAll();
        seed(poolSize);
    }

    public void seed(int poolSize) {
        for (int i = 1; i <= poolSize; i++) {
            String proxyId = "proxy-" + i;
            redisTemplate.opsForList().rightPush(v1OrderKey(), proxyId);
            redisTemplate.opsForList().rightPush(v2PoolKey(), proxyId);
            redisTemplate.opsForHash().put(v1ResourceKey(proxyId), "ownerApp", "");
            redisTemplate.opsForHash().put(v1ResourceKey(proxyId), "requestId", "");
            redisTemplate.opsForHash().put(v1ResourceKey(proxyId), "clientName", "");
            redisTemplate.opsForHash().put(v1ResourceKey(proxyId), "clientIp", "");
        }
    }

    public void clearAll() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public StringRedisTemplate redis() {
        return redisTemplate;
    }

    public String v1OrderKey() {
        return PREFIX + "v1:order";
    }

    public String v1ResourceKey(String proxyId) {
        return PREFIX + "v1:resource:" + proxyId;
    }

    public String v2PoolKey() {
        return PREFIX + "v2:pool";
    }

    public String v2BorrowedKey(String proxyId) {
        return PREFIX + "v2:borrowed:" + proxyId;
    }

    public String v2LeaseKey(String proxyId) {
        return PREFIX + "v2:lease:" + proxyId;
    }

    public String v2RequestKey(String requestId) {
        return PREFIX + "v2:req:" + requestId;
    }

    public String v2ReturnRequestKey(String requestId) {
        return PREFIX + "v2:return:req:" + requestId;
    }
}
