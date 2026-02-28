package com.yowon.psc.common.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QueryMetricsRecorderTest {

    @Test
    void snapshotShouldContainRecordedValues() {
        QueryMetricsRecorder recorder = new QueryMetricsRecorder();

        recorder.record("baseline", 12, 41);
        recorder.record("baseline", 8, 59);

        QueryMetricSnapshot snapshot = recorder.snapshot("baseline");

        assertThat(snapshot.getSampleCount()).isEqualTo(2);
        assertThat(snapshot.getTotalSqlCount()).isEqualTo(20);
        assertThat(snapshot.getAverageLatencyMs()).isEqualTo(50.0d);
        assertThat(snapshot.getP95LatencyMs()).isEqualTo(59L);
    }

    @Test
    void resetShouldClearRecordedValues() {
        QueryMetricsRecorder recorder = new QueryMetricsRecorder();

        recorder.record("optimized", 2, 11);
        recorder.reset("optimized");

        QueryMetricSnapshot snapshot = recorder.snapshot("optimized");

        assertThat(snapshot.getSampleCount()).isZero();
        assertThat(snapshot.getTotalSqlCount()).isZero();
        assertThat(snapshot.getAverageLatencyMs()).isZero();
        assertThat(snapshot.getP95LatencyMs()).isZero();
    }
}
