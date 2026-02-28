package com.yowon.psc.common.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueryMetricsRecorder {

    private final Map<String, List<MetricPoint>> metricsByScenario = new ConcurrentHashMap<>();

    public void record(String scenario, long sqlCount, long latencyMs) {
        metricsByScenario
            .computeIfAbsent(scenario, ignored -> Collections.synchronizedList(new ArrayList<>()))
            .add(new MetricPoint(sqlCount, latencyMs));
    }

    public QueryMetricSnapshot snapshot(String scenario) {
        List<MetricPoint> points = metricsByScenario.getOrDefault(scenario, Collections.emptyList());
        if (points.isEmpty()) {
            return new QueryMetricSnapshot(0L, 0L, 0.0d, 0L);
        }

        List<Long> latencies = new ArrayList<>(points.size());
        long totalSqlCount = 0L;
        long totalLatency = 0L;

        for (MetricPoint point : points) {
            totalSqlCount += point.sqlCount;
            totalLatency += point.latencyMs;
            latencies.add(point.latencyMs);
        }

        Collections.sort(latencies);
        int p95Index = Math.max(0, (int) Math.ceil(latencies.size() * 0.95d) - 1);
        long p95Latency = latencies.get(p95Index);
        double avgLatency = (double) totalLatency / (double) points.size();

        return new QueryMetricSnapshot(points.size(), totalSqlCount, avgLatency, p95Latency);
    }

    public void reset(String scenario) {
        metricsByScenario.remove(scenario);
    }

    private static class MetricPoint {
        private final long sqlCount;
        private final long latencyMs;

        private MetricPoint(long sqlCount, long latencyMs) {
            this.sqlCount = sqlCount;
            this.latencyMs = latencyMs;
        }
    }
}
