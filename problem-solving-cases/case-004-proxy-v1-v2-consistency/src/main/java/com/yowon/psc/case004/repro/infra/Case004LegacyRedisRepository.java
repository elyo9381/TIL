package com.yowon.psc.case004.repro.infra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class Case004LegacyRedisRepository {

    private final Case004ScenarioRedisSupport support;

    public Case004LegacyRedisRepository(Case004ScenarioRedisSupport support) {
        this.support = support;
    }

    public String borrow(
        String domain,
        String appName,
        String clientName,
        String clientIp,
        String requestId,
        boolean failAfterWrite,
        int artificialDelayMs
    ) {
        List<String> resources = support.redis().opsForList().range(support.legacyOrderKey(domain), 0, -1);
        if (resources == null || resources.isEmpty()) {
            return null;
        }

        for (String resourceId : resources) {
            Map<String, Map<String, String>> owners = support.readOwnerMap(resourceId);
            if (owners.containsKey(appName)) {
                continue;
            }

            if (artificialDelayMs > 0) {
                sleep(artificialDelayMs);
            }

            Map<String, String> ownerInfo = new HashMap<>();
            ownerInfo.put("appName", appName);
            ownerInfo.put("clientName", clientName);
            ownerInfo.put("clientIp", clientIp);
            ownerInfo.put("requestId", requestId);
            owners.put(appName, ownerInfo);
            support.writeLegacyMaps(resourceId, owners, support.readLastReleasedMap(resourceId));

            if (failAfterWrite) {
                throw new IllegalStateException("Simulated transport failure after write");
            }
            return resourceId;
        }

        return null;
    }

    public int countActiveLeases(String domain, String appName) {
        List<String> resources = support.redis().opsForList().range(support.legacyOrderKey(domain), 0, -1);
        if (resources == null || resources.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String resource : resources) {
            Map<String, Map<String, String>> owners = support.readOwnerMap(resource);
            if (owners.containsKey(appName)) {
                count++;
            }
        }
        return count;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
