package com.yowon.psc.case004.repro.infra;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Repository
public class Case004TargetRedisRepository {

    private static final String BORROW_SCRIPT =
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

    private static final String RETURN_SCRIPT =
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

    private final Case004ScenarioRedisSupport support;

    public Case004TargetRedisRepository(Case004ScenarioRedisSupport support) {
        this.support = support;
    }

    public String borrow(
        String domain,
        String appName,
        String clientName,
        String clientIp,
        String requestId,
        boolean failAfterWrite
    ) {
        String resource = support.redis().execute(
            new DefaultRedisScript<>(BORROW_SCRIPT, String.class),
            List.of(support.targetPoolKey(domain), support.targetRequestKey(requestId)),
            support.targetBorrowedPrefix(domain),
            support.targetLeasePrefix(domain),
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

    public String returnLease(
        String domain,
        String resourceId,
        String appName,
        String clientName,
        String clientIp,
        String returnRequestId
    ) {
        return support.redis().execute(
            new DefaultRedisScript<>(RETURN_SCRIPT, String.class),
            List.of(
                support.targetReturnRequestKey(returnRequestId),
                support.targetBorrowedKey(domain, resourceId),
                support.targetLeaseKey(domain, resourceId)
            ),
            returnRequestId,
            appName,
            clientName,
            clientIp,
            "600"
        );
    }

    public int countActiveLeases(String domain, String appName) {
        List<String> resources = support.redis().opsForList().range(support.targetPoolKey(domain), 0, -1);
        if (resources == null || resources.isEmpty()) {
            return 0;
        }
        Set<String> distinct = new HashSet<>(resources);
        int count = 0;
        for (String resource : distinct) {
            Object value = support.redis().opsForHash().get(support.targetLeaseKey(domain, resource), "appName");
            if (appName.equals(value)) {
                count++;
            }
        }
        return count;
    }
}
