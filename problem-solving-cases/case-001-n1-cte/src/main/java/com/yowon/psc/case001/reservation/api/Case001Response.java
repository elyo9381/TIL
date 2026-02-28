package com.yowon.psc.case001.reservation.api;

import com.yowon.psc.case001.reservation.application.ReservationCaseResponse;
import com.yowon.psc.common.metrics.QueryMetricSnapshot;
import java.util.List;

public class Case001Response {

    private final String scenario;
    private final QueryMetricSnapshot metrics;
    private final List<ReservationCaseResponse> data;

    public Case001Response(String scenario, QueryMetricSnapshot metrics, List<ReservationCaseResponse> data) {
        this.scenario = scenario;
        this.metrics = metrics;
        this.data = data;
    }

    public String getScenario() {
        return scenario;
    }

    public QueryMetricSnapshot getMetrics() {
        return metrics;
    }

    public List<ReservationCaseResponse> getData() {
        return data;
    }
}
