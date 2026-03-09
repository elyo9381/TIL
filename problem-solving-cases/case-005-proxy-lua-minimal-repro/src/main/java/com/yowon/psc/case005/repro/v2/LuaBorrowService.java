package com.yowon.psc.case005.repro.v2;

import com.yowon.psc.case005.repro.infra.ProxyPoolSeedSupport;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class LuaBorrowService {

    private final ProxyPoolSeedSupport seedSupport;
    private final DefaultRedisScript<String> borrowScript;
    private final DefaultRedisScript<String> returnScript;
    private final int leaseTtlSeconds;

    public LuaBorrowService(
        ProxyPoolSeedSupport seedSupport,
        @Value("${app.case005.v2.lease-ttl-seconds:300}") int leaseTtlSeconds
    ) {
        this.seedSupport = seedSupport;
        this.leaseTtlSeconds = Math.max(1, leaseTtlSeconds);
        this.borrowScript = new DefaultRedisScript<>();
        this.borrowScript.setLocation(new ClassPathResource("lua/borrow.lua"));
        this.borrowScript.setResultType(String.class);

        this.returnScript = new DefaultRedisScript<>();
        this.returnScript.setLocation(new ClassPathResource("lua/return.lua"));
        this.returnScript.setResultType(String.class);
    }

    public String borrow(String appName, String clientName, String clientIp, String requestId) {
        return borrow(appName, clientName, clientIp, requestId, false);
    }

    public String borrow(String appName, String clientName, String clientIp, String requestId, boolean failAfterWrite) {
        String proxyId = seedSupport.redis().execute(
            borrowScript,
            List.of(seedSupport.v2PoolKey(), seedSupport.v2RequestKey(requestId)),
            "repro:case005:v2:borrowed:",
            requestId,
            "repro:case005:v2:lease:",
            String.valueOf(leaseTtlSeconds),
            String.valueOf(leaseTtlSeconds),
            appName,
            clientName,
            clientIp
        );

        if ("NONE".equals(proxyId)) {
            return null;
        }
        if (failAfterWrite && proxyId != null) {
            throw new IllegalStateException("Simulated transport failure after write");
        }
        return proxyId;
    }

    public String returnBorrowed(String proxyId, String appName, String clientName, String clientIp, String requestId) {
        return seedSupport.redis().execute(
            returnScript,
            List.of(
                seedSupport.v2ReturnRequestKey(requestId),
                seedSupport.v2BorrowedKey(proxyId),
                seedSupport.v2LeaseKey(proxyId)
            ),
            requestId,
            appName,
            clientName,
            clientIp,
            String.valueOf(leaseTtlSeconds)
        );
    }

    public int activeLeaseCount() {
        List<String> pool = seedSupport.redis().opsForList().range(seedSupport.v2PoolKey(), 0, -1);
        if (pool == null || pool.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String proxyId : pool) {
            Boolean exists = seedSupport.redis().hasKey(seedSupport.v2BorrowedKey(proxyId));
            if (Boolean.TRUE.equals(exists)) {
                count++;
            }
        }
        return count;
    }
}
