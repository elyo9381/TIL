package com.yowon.psc.case003.repro.application;

import com.yowon.psc.case003.repro.domain.JoinHintBenchmarkReport;
import com.yowon.psc.case003.repro.domain.JoinHintReproReport;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JoinHintBenchmarkService {

    private final JoinHintReproService reproService;

    public JoinHintBenchmarkService(JoinHintReproService reproService) {
        this.reproService = reproService;
    }

    public JoinHintBenchmarkReport benchmark(String serviceType, int limit, int iterations) {
        int safeLimit = Math.max(1, limit);
        int safeIterations = Math.max(1, iterations);

        List<Long> withoutLatencies = new ArrayList<>(safeIterations);
        List<Long> withLatencies = new ArrayList<>(safeIterations);

        for (int i = 0; i < safeIterations; i++) {
            JoinHintReproReport report = reproService.run(serviceType, safeLimit);
            withoutLatencies.add(report.getWithoutHintLatencyMs());
            withLatencies.add(report.getWithHintLatencyMs());
        }

        return new JoinHintBenchmarkReport(
            serviceType,
            safeLimit,
            safeIterations,
            toStats(withoutLatencies),
            toStats(withLatencies)
        );
    }

    private JoinHintBenchmarkReport.LatencyStats toStats(List<Long> latencies) {
        long sum = 0L;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        for (Long latency : latencies) {
            long value = latency == null ? 0L : latency;
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        int count = latencies.size();
        double average = count == 0 ? 0.0 : (double) sum / count;
        long safeMin = count == 0 ? 0L : min;
        long safeMax = count == 0 ? 0L : max;
        return new JoinHintBenchmarkReport.LatencyStats(average, safeMin, safeMax, count);
    }
}
