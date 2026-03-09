package com.yowon.psc.case004.repro.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowon.psc.case004.repro.domain.Case004Profile;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class Case004ScenarioRedisSupport {

    private static final String PREFIX = "repro:case004:";
    private static final String RUN_LOCK_KEY = "repro:case004-lock:run";
    private static final String RELEASE_LOCK_SCRIPT =
        "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
        "  return redis.call('DEL', KEYS[1]) " +
        "else " +
        "  return 0 " +
        "end";
    private static final String REFRESH_LOCK_SCRIPT =
        "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
        "  return redis.call('PEXPIRE', KEYS[1], ARGV[2]) " +
        "else " +
        "  return 0 " +
        "end";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public Case004ScenarioRedisSupport(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void resetScenario(Case004Profile profile, String domain, int poolSize) {
        clearAll();
        seed(domain, poolSize);
    }

    public void seed(String domain, int poolSize) {
        String legacyOrderKey = legacyOrderKey(domain);
        String targetPoolKey = targetPoolKey(domain);
        for (int i = 1; i <= poolSize; i++) {
            String resourceId = "proxy-" + i;
            String resourceKey = legacyResourceKey(resourceId);
            redisTemplate.opsForHash().put(resourceKey, "activeOwnerMapJson", "{}");
            redisTemplate.opsForHash().put(resourceKey, "lastReleasedAtMapJson", "{}");
            redisTemplate.opsForList().rightPush(legacyOrderKey, resourceId);
            redisTemplate.opsForList().rightPush(targetPoolKey, resourceId);
        }
    }

    public void clearAll() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public String tryAcquireRunLock(Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(RUN_LOCK_KEY, token, ttl);
        return Boolean.TRUE.equals(acquired) ? token : null;
    }

    public void releaseRunLock(String token) {
        if (token == null) {
            return;
        }
        redisTemplate.execute(
            new DefaultRedisScript<>(RELEASE_LOCK_SCRIPT, Long.class),
            java.util.List.of(RUN_LOCK_KEY),
            token
        );
    }

    public boolean refreshRunLock(String token, Duration ttl) {
        if (token == null || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return false;
        }
        Long refreshed = redisTemplate.execute(
            new DefaultRedisScript<>(REFRESH_LOCK_SCRIPT, Long.class),
            java.util.List.of(RUN_LOCK_KEY),
            token,
            String.valueOf(ttl.toMillis())
        );
        return refreshed != null && refreshed > 0;
    }

    Map<String, Map<String, String>> readOwnerMap(String resourceId) {
        Object json = redisTemplate.opsForHash().get(legacyResourceKey(resourceId), "activeOwnerMapJson");
        if (json == null || String.valueOf(json).isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(String.valueOf(json), new TypeReference<Map<String, Map<String, String>>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    Map<String, Long> readLastReleasedMap(String resourceId) {
        Object json = redisTemplate.opsForHash().get(legacyResourceKey(resourceId), "lastReleasedAtMapJson");
        if (json == null || String.valueOf(json).isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(String.valueOf(json), new TypeReference<Map<String, Long>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    void writeLegacyMaps(String resourceId, Map<String, Map<String, String>> owners, Map<String, Long> releasedAt) {
        try {
            redisTemplate.opsForHash().put(legacyResourceKey(resourceId), "activeOwnerMapJson", objectMapper.writeValueAsString(owners));
            redisTemplate.opsForHash().put(legacyResourceKey(resourceId), "lastReleasedAtMapJson", objectMapper.writeValueAsString(releasedAt));
        } catch (JsonProcessingException ignored) {
            // no-op for repro module
        }
    }

    RedisTemplate<String, String> redis() {
        return redisTemplate;
    }

    String legacyOrderKey(String domain) {
        return PREFIX + "v1:order:" + domain;
    }

    String legacyResourceKey(String resourceId) {
        return PREFIX + "v1:resource:" + resourceId;
    }

    String targetPoolKey(String domain) {
        return PREFIX + "v2:pool:" + domain;
    }

    String targetBorrowedPrefix(String domain) {
        return PREFIX + "v2:borrowed:" + domain + ":";
    }

    String targetBorrowedKey(String domain, String resourceId) {
        return targetBorrowedPrefix(domain) + resourceId;
    }

    String targetLeasePrefix(String domain) {
        return PREFIX + "v2:lease:" + domain + ":";
    }

    String targetLeaseKey(String domain, String resourceId) {
        return targetLeasePrefix(domain) + resourceId;
    }

    String targetRequestKey(String requestId) {
        return PREFIX + "v2:req:" + requestId;
    }

    String targetReturnRequestKey(String requestId) {
        return PREFIX + "v2:return:req:" + requestId;
    }
}
