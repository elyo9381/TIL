package com.yowon.psc.case004.repro.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowon.psc.case004.repro.domain.Case004Profile;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Repository
public class Case004RedisRepository {

    private static final String PREFIX = "repro:case004:";
    // Keep this key outside PREFIX so scenario reset does not delete the global run lock.
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

    private static final String BORROW_V2_SCRIPT =
        "local existing = redis.call('GET', KEYS[2]); " +
        "if existing then return existing end; " +
        "local poolSize = redis.call('LLEN', KEYS[1]); " +
        "if poolSize <= 0 then redis.call('SET', KEYS[2], 'NONE', 'EX', tonumber(ARGV[9])); return 'NONE' end; " +
        "for i=1,poolSize do " +
        "  local resource = redis.call('RPOPLPUSH', KEYS[1], KEYS[1]); " +
        "  local borrowedKey = ARGV[1] .. resource; " +
        "  if redis.call('SETNX', borrowedKey, ARGV[3]) == 1 then " +
        "    redis.call('EXPIRE', borrowedKey, tonumber(ARGV[8])); " +
        "    local leaseKey = ARGV[2] .. resource; " +
        "    redis.call('HSET', leaseKey, 'appName', ARGV[4], 'clientName', ARGV[5], 'clientIp', ARGV[6], 'requestId', ARGV[3], 'borrowedAt', ARGV[7]); " +
        "    redis.call('EXPIRE', leaseKey, tonumber(ARGV[10])); " +
        "    redis.call('SET', KEYS[2], resource, 'EX', tonumber(ARGV[9])); " +
        "    return resource; " +
        "  end; " +
        "end; " +
        "redis.call('SET', KEYS[2], 'NONE', 'EX', tonumber(ARGV[9])); " +
        "return 'NONE';";

    private static final String RETURN_V2_SCRIPT =
        "local existing = redis.call('GET', KEYS[1]); " +
        "if existing then return existing end; " +
        "if redis.call('EXISTS', KEYS[2]) == 0 then redis.call('SET', KEYS[1], 'NOT_BORROWED', 'EX', tonumber(ARGV[5])); return 'NOT_BORROWED' end; " +
        "local appName = redis.call('HGET', KEYS[3], 'appName'); " +
        "local clientName = redis.call('HGET', KEYS[3], 'clientName'); " +
        "local clientIp = redis.call('HGET', KEYS[3], 'clientIp'); " +
        "if appName ~= ARGV[2] or clientName ~= ARGV[3] or clientIp ~= ARGV[4] then " +
        "  redis.call('SET', KEYS[1], 'OWNER_MISMATCH', 'EX', tonumber(ARGV[5])); " +
        "  return 'OWNER_MISMATCH'; " +
        "end; " +
        "redis.call('DEL', KEYS[2]); redis.call('DEL', KEYS[3]); " +
        "redis.call('SET', KEYS[1], 'RETURNED', 'EX', tonumber(ARGV[5])); " +
        "return 'RETURNED';";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public Case004RedisRepository(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void resetScenario(Case004Profile profile, String domain, int poolSize) {
        clearAll();
        seed(domain, poolSize);
        if (profile == Case004Profile.V1_P3) {
            redisTemplate.opsForValue().set(v1IdxKey(domain, "default"), "0");
        }
    }

    public void seed(String domain, int poolSize) {
        String orderKey = v1OrderKey(domain);
        String v2PoolKey = v2PoolKey(domain);
        for (int i = 1; i <= poolSize; i++) {
            String resourceId = "proxy-" + i;
            String resourceKey = v1ResourceKey(resourceId);
            redisTemplate.opsForHash().put(resourceKey, "activeOwnerMapJson", "{}");
            redisTemplate.opsForHash().put(resourceKey, "lastReleasedAtMapJson", "{}");
            redisTemplate.opsForList().rightPush(orderKey, resourceId);
            redisTemplate.opsForList().rightPush(v2PoolKey, resourceId);
        }
    }

    public String borrowV1(
        String domain,
        String appName,
        String clientName,
        String clientIp,
        String requestId,
        int cooldownMinutes,
        boolean phase3,
        boolean failAfterWrite,
        int artificialDelayMs
    ) {
        List<String> resources = redisTemplate.opsForList().range(v1OrderKey(domain), 0, -1);
        if (resources == null || resources.isEmpty()) {
            return null;
        }

        int startIdx = 0;
        if (phase3) {
            startIdx = parseInt(redisTemplate.opsForValue().get(v1IdxKey(domain, appName)));
        }

        long nowMillis = System.currentTimeMillis();

        for (int i = 0; i < resources.size(); i++) {
            if (phase3 && i < startIdx) {
                continue;
            }
            String resourceId = resources.get(i);
            Map<String, Map<String, String>> owners = readOwnerMap(resourceId);
            Map<String, Long> releasedAt = readLastReleasedMap(resourceId);
            Long lastReleased = releasedAt.get(appName);
            boolean cooldownElapsed = lastReleased == null || nowMillis > lastReleased + cooldownMinutes * 60_000L;

            if (!owners.containsKey(appName) && cooldownElapsed) {
                if (artificialDelayMs > 0) {
                    sleep(artificialDelayMs);
                }
                Map<String, String> ownerInfo = new HashMap<>();
                ownerInfo.put("appName", appName);
                ownerInfo.put("clientName", clientName);
                ownerInfo.put("clientIp", clientIp);
                ownerInfo.put("requestId", requestId);
                owners.put(appName, ownerInfo);
                writeMaps(resourceId, owners, releasedAt);

                if (phase3) {
                    redisTemplate.opsForValue().set(v1IdxKey(domain, appName), String.valueOf(i + 1));
                    redisTemplate.opsForList().rightPush(v1IpListKey(domain, appName), resourceId);
                }

                if (failAfterWrite) {
                    throw new IllegalStateException("Simulated transport failure after write");
                }
                return resourceId;
            }
        }

        if (phase3) {
            redisTemplate.opsForValue().set(v1IdxKey(domain, appName), "0");
        }
        return null;
    }

    public String returnV1(
        String domain,
        String resourceId,
        String appName,
        String clientName,
        String clientIp,
        boolean phase3
    ) {
        Map<String, Map<String, String>> owners = readOwnerMap(resourceId);
        Map<String, String> owner = owners.get(appName);

        if (owner != null && equalsAgent(owner, appName, clientName, clientIp)) {
            owners.remove(appName);
            Map<String, Long> releasedAt = readLastReleasedMap(resourceId);
            releasedAt.put(appName, System.currentTimeMillis());
            writeMaps(resourceId, owners, releasedAt);

            if (phase3) {
                redisTemplate.opsForList().remove(v1IpListKey(domain, appName), 1, resourceId);
            }
            return "RETURNED";
        }
        return "SKIPPED";
    }

    public String borrowV2(
        String domain,
        String appName,
        String clientName,
        String clientIp,
        String requestId,
        boolean failAfterWrite
    ) {
        String reqKey = v2RequestKey(requestId);
        String resource = redisTemplate.execute(
            new DefaultRedisScript<>(BORROW_V2_SCRIPT, String.class),
            List.of(v2PoolKey(domain), reqKey),
            v2BorrowedPrefix(domain),
            v2LeasePrefix(domain),
            requestId,
            appName,
            clientName,
            clientIp,
            String.valueOf(Instant.now().toEpochMilli()),
            "300",
            "600",
            "600"
        );

        if ("NONE".equals(resource)) {
            return null;
        }
        if (failAfterWrite && resource != null) {
            throw new IllegalStateException("Simulated transport failure after write");
        }
        return resource;
    }

    public String returnV2(
        String domain,
        String resourceId,
        String appName,
        String clientName,
        String clientIp,
        String returnRequestId
    ) {
        return redisTemplate.execute(
            new DefaultRedisScript<>(RETURN_V2_SCRIPT, String.class),
            List.of(
                v2ReturnRequestKey(returnRequestId),
                v2BorrowedKey(domain, resourceId),
                v2LeaseKey(domain, resourceId)
            ),
            returnRequestId,
            appName,
            clientName,
            clientIp,
            "600"
        );
    }

    public int countActiveV1Leases(String domain, String appName) {
        List<String> resources = redisTemplate.opsForList().range(v1OrderKey(domain), 0, -1);
        if (resources == null || resources.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String resource : resources) {
            Map<String, Map<String, String>> owners = readOwnerMap(resource);
            if (owners.containsKey(appName)) {
                count++;
            }
        }
        return count;
    }

    public int countActiveV2Leases(String domain, String appName) {
        List<String> resources = redisTemplate.opsForList().range(v2PoolKey(domain), 0, -1);
        if (resources == null || resources.isEmpty()) {
            return 0;
        }
        Set<String> distinct = new HashSet<>(resources);
        int count = 0;
        for (String resource : distinct) {
            Object value = redisTemplate.opsForHash().get(v2LeaseKey(domain, resource), "appName");
            if (appName.equals(value)) {
                count++;
            }
        }
        return count;
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
            List.of(RUN_LOCK_KEY),
            token
        );
    }

    public boolean refreshRunLock(String token, Duration ttl) {
        if (token == null || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return false;
        }
        Long refreshed = redisTemplate.execute(
            new DefaultRedisScript<>(REFRESH_LOCK_SCRIPT, Long.class),
            List.of(RUN_LOCK_KEY),
            token,
            String.valueOf(ttl.toMillis())
        );
        return refreshed != null && refreshed > 0;
    }

    private Map<String, Map<String, String>> readOwnerMap(String resourceId) {
        String key = v1ResourceKey(resourceId);
        Object json = redisTemplate.opsForHash().get(key, "activeOwnerMapJson");
        if (json == null || String.valueOf(json).isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(String.valueOf(json), new TypeReference<Map<String, Map<String, String>>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    private Map<String, Long> readLastReleasedMap(String resourceId) {
        String key = v1ResourceKey(resourceId);
        Object json = redisTemplate.opsForHash().get(key, "lastReleasedAtMapJson");
        if (json == null || String.valueOf(json).isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(String.valueOf(json), new TypeReference<Map<String, Long>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    private void writeMaps(String resourceId, Map<String, Map<String, String>> owners, Map<String, Long> releasedAt) {
        String key = v1ResourceKey(resourceId);
        try {
            redisTemplate.opsForHash().put(key, "activeOwnerMapJson", objectMapper.writeValueAsString(owners));
            redisTemplate.opsForHash().put(key, "lastReleasedAtMapJson", objectMapper.writeValueAsString(releasedAt));
        } catch (JsonProcessingException ignored) {
            // no-op for repro module
        }
    }

    private static boolean equalsAgent(Map<String, String> owner, String appName, String clientName, String clientIp) {
        if (owner == null) {
            return false;
        }
        return appName.equals(owner.get("appName"))
            && clientName.equals(owner.get("clientName"))
            && clientIp.equals(owner.get("clientIp"));
    }

    private static int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String v1OrderKey(String domain) {
        return PREFIX + "v1:order:" + domain;
    }

    private static String v1ResourceKey(String resourceId) {
        return PREFIX + "v1:resource:" + resourceId;
    }

    private static String v1IdxKey(String domain, String appName) {
        return PREFIX + "v1:p3:idx:" + domain + ":" + appName;
    }

    private static String v1IpListKey(String domain, String appName) {
        return PREFIX + "v1:p3:iplist:" + domain + ":" + appName;
    }

    private static String v2PoolKey(String domain) {
        return PREFIX + "v2:pool:" + domain;
    }

    private static String v2BorrowedPrefix(String domain) {
        return PREFIX + "v2:borrowed:" + domain + ":";
    }

    private static String v2BorrowedKey(String domain, String resourceId) {
        return v2BorrowedPrefix(domain) + resourceId;
    }

    private static String v2LeasePrefix(String domain) {
        return PREFIX + "v2:lease:" + domain + ":";
    }

    private static String v2LeaseKey(String domain, String resourceId) {
        return v2LeasePrefix(domain) + resourceId;
    }

    private static String v2RequestKey(String requestId) {
        return PREFIX + "v2:req:" + requestId;
    }

    private static String v2ReturnRequestKey(String requestId) {
        return PREFIX + "v2:return:req:" + requestId;
    }
}
