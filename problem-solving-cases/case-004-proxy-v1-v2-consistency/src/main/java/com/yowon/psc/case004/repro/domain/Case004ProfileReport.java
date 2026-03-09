package com.yowon.psc.case004.repro.domain;

public class Case004ProfileReport {

    private final String profile;
    private final long duplicateLeaseCount;
    private final long orphanLeaseCount;
    private final long idempotencyMismatchCount;

    public Case004ProfileReport(
        String profile,
        long duplicateLeaseCount,
        long orphanLeaseCount,
        long idempotencyMismatchCount
    ) {
        this.profile = profile;
        this.duplicateLeaseCount = duplicateLeaseCount;
        this.orphanLeaseCount = orphanLeaseCount;
        this.idempotencyMismatchCount = idempotencyMismatchCount;
    }

    public String getProfile() {
        return profile;
    }

    public long getDuplicateLeaseCount() {
        return duplicateLeaseCount;
    }

    public long getOrphanLeaseCount() {
        return orphanLeaseCount;
    }

    public long getIdempotencyMismatchCount() {
        return idempotencyMismatchCount;
    }
}
