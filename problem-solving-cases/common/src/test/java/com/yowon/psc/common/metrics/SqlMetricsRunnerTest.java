package com.yowon.psc.common.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SqlMetricsRunnerTest {

    private final SqlMetricsRunner sqlMetricsRunner = new SqlMetricsRunner();

    @Test
    void measureShouldReturnQueryCountAndLatency() {
        SqlMeasuredExecution<String> execution = sqlMetricsRunner.measure(() -> {
            SqlQueryCountHolder.addCount(3);
            return "ok";
        });

        assertThat(execution.getResult()).isEqualTo("ok");
        assertThat(execution.getSqlCount()).isEqualTo(3L);
        assertThat(execution.getLatencyMs()).isPositive();
    }

    @Test
    void measureShouldResetCountForEachCall() {
        SqlMeasuredExecution<Integer> first = sqlMetricsRunner.measure(() -> {
            SqlQueryCountHolder.addCount(2);
            return 1;
        });
        SqlMeasuredExecution<Integer> second = sqlMetricsRunner.measure(() -> {
            SqlQueryCountHolder.addCount(1);
            return 2;
        });

        assertThat(first.getSqlCount()).isEqualTo(2L);
        assertThat(second.getSqlCount()).isEqualTo(1L);
    }

    @Test
    void measureShouldClearThreadLocalEvenWhenExceptionOccurs() {
        assertThatThrownBy(() -> sqlMetricsRunner.measure(() -> {
            SqlQueryCountHolder.addCount(5);
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);

        SqlMeasuredExecution<Integer> next = sqlMetricsRunner.measure(() -> 7);
        assertThat(next.getSqlCount()).isZero();
        assertThat(next.getResult()).isEqualTo(7);
    }
}
