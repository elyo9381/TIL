package com.yowon.psc.case005.repro.v1;

import com.yowon.psc.case005.repro.infra.ProxyPoolSeedSupport;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LegacyBorrowService {

    private final ProxyPoolSeedSupport seedSupport;
    private final int artificialRaceDelayMs;

    public LegacyBorrowService(
        ProxyPoolSeedSupport seedSupport,
        @Value("${app.case005.v1.artificial-race-delay-ms:0}") int artificialRaceDelayMs
    ) {
        this.seedSupport = seedSupport;
        this.artificialRaceDelayMs = Math.max(0, artificialRaceDelayMs);
    }

    public String borrow(String appName, String clientName, String clientIp, String requestId) {
        return borrow(appName, clientName, clientIp, requestId, false);
    }

    public String borrow(String appName, String clientName, String clientIp, String requestId, boolean failAfterWrite) {
        List<String> pool = seedSupport.redis().opsForList().range(seedSupport.v1OrderKey(), 0, -1);
        if (pool == null || pool.isEmpty()) {
            return null;
        }

        for (String proxyId : pool) {
            Object owner = seedSupport.redis().opsForHash().get(seedSupport.v1ResourceKey(proxyId), "ownerApp");
            if (owner != null && !String.valueOf(owner).isBlank()) {
                continue;
            }

            sleep(artificialRaceDelayMs);
            seedSupport.redis().opsForHash().put(seedSupport.v1ResourceKey(proxyId), "ownerApp", appName);
            seedSupport.redis().opsForHash().put(seedSupport.v1ResourceKey(proxyId), "requestId", requestId);
            seedSupport.redis().opsForHash().put(seedSupport.v1ResourceKey(proxyId), "clientName", clientName);
            seedSupport.redis().opsForHash().put(seedSupport.v1ResourceKey(proxyId), "clientIp", clientIp);

            if (failAfterWrite) {
                throw new IllegalStateException("Simulated transport failure after write");
            }
            return proxyId;
        }

        return null;
    }

    public int activeLeaseCount() {
        List<String> pool = seedSupport.redis().opsForList().range(seedSupport.v1OrderKey(), 0, -1);
        if (pool == null || pool.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String proxyId : pool) {
            Object owner = seedSupport.redis().opsForHash().get(seedSupport.v1ResourceKey(proxyId), "ownerApp");
            if (owner != null && !String.valueOf(owner).isBlank()) {
                count++;
            }
        }
        return count;
    }

    private static void sleep(int millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
