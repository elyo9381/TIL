package com.yowon.psc.case003.repro.domain;

import java.util.List;

public class JoinHintReproReport {

    private final String serviceType;
    private final int limit;
    private final long withoutHintLatencyMs;
    private final long withHintLatencyMs;
    private final int withoutHintResultSize;
    private final int withHintResultSize;
    private final List<Long> withoutHintSampleIds;
    private final List<Long> withHintSampleIds;
    private final List<ExplainRow> withoutHintPlan;
    private final List<ExplainRow> withHintPlan;
    private final ExplainJsonPair explainJson;

    public JoinHintReproReport(
        String serviceType,
        int limit,
        long withoutHintLatencyMs,
        long withHintLatencyMs,
        int withoutHintResultSize,
        int withHintResultSize,
        List<Long> withoutHintSampleIds,
        List<Long> withHintSampleIds,
        List<ExplainRow> withoutHintPlan,
        List<ExplainRow> withHintPlan,
        ExplainJsonPair explainJson
    ) {
        this.serviceType = serviceType;
        this.limit = limit;
        this.withoutHintLatencyMs = withoutHintLatencyMs;
        this.withHintLatencyMs = withHintLatencyMs;
        this.withoutHintResultSize = withoutHintResultSize;
        this.withHintResultSize = withHintResultSize;
        this.withoutHintSampleIds = withoutHintSampleIds;
        this.withHintSampleIds = withHintSampleIds;
        this.withoutHintPlan = withoutHintPlan;
        this.withHintPlan = withHintPlan;
        this.explainJson = explainJson;
    }

    public String getServiceType() {
        return serviceType;
    }

    public int getLimit() {
        return limit;
    }

    public long getWithoutHintLatencyMs() {
        return withoutHintLatencyMs;
    }

    public long getWithHintLatencyMs() {
        return withHintLatencyMs;
    }

    public int getWithoutHintResultSize() {
        return withoutHintResultSize;
    }

    public int getWithHintResultSize() {
        return withHintResultSize;
    }

    public List<Long> getWithoutHintSampleIds() {
        return withoutHintSampleIds;
    }

    public List<Long> getWithHintSampleIds() {
        return withHintSampleIds;
    }

    public List<ExplainRow> getWithoutHintPlan() {
        return withoutHintPlan;
    }

    public List<ExplainRow> getWithHintPlan() {
        return withHintPlan;
    }

    public ExplainJsonPair getExplainJson() {
        return explainJson;
    }

    public String getWithoutHintExplainJson() {
        return explainJson == null ? null : explainJson.getWithoutHint();
    }

    public String getWithHintExplainJson() {
        return explainJson == null ? null : explainJson.getWithHint();
    }
}
