package com.yowon.psc.case001.reservation.api;

import com.yowon.psc.case001.reservation.application.BaselineReservationService;
import com.yowon.psc.case001.reservation.application.OptimizedReservationService;
import com.yowon.psc.case001.reservation.application.ReservationCaseResponse;
import com.yowon.psc.common.metrics.QueryMetricSnapshot;
import com.yowon.psc.common.metrics.SqlMeasuredExecution;
import com.yowon.psc.common.metrics.SqlMetricsRunner;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/case-001/reservations")
public class Case001Controller {

    private final BaselineReservationService baselineReservationService;
    private final OptimizedReservationService optimizedReservationService;
    private final SqlMetricsRunner sqlMetricsRunner;

    public Case001Controller(
        BaselineReservationService baselineReservationService,
        OptimizedReservationService optimizedReservationService,
        SqlMetricsRunner sqlMetricsRunner
    ) {
        this.baselineReservationService = baselineReservationService;
        this.optimizedReservationService = optimizedReservationService;
        this.sqlMetricsRunner = sqlMetricsRunner;
    }

    @GetMapping("/baseline")
    public Case001Response baseline(
        @RequestParam("agno") Long agno,
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        SqlMeasuredExecution<List<ReservationCaseResponse>> measured = sqlMetricsRunner.measure(
            () -> baselineReservationService.load(agno, limit)
        );
        return new Case001Response("baseline", toSingleRequestMetric(measured), measured.getResult());
    }

    @GetMapping("/optimized")
    public Case001Response optimized(
        @RequestParam("agno") Long agno,
        @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        SqlMeasuredExecution<List<ReservationCaseResponse>> measured = sqlMetricsRunner.measure(
            () -> optimizedReservationService.load(agno, limit)
        );
        return new Case001Response("optimized", toSingleRequestMetric(measured), measured.getResult());
    }

    private QueryMetricSnapshot toSingleRequestMetric(SqlMeasuredExecution<?> measuredExecution) {
        return new QueryMetricSnapshot(
            1L,
            measuredExecution.getSqlCount(),
            (double) measuredExecution.getLatencyMs(),
            measuredExecution.getLatencyMs()
        );
    }
}
