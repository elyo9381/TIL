package com.yowon.psc.case003.repro.application;

import com.yowon.psc.case003.repro.domain.ExplainRow;
import com.yowon.psc.case003.repro.domain.ExplainJsonPair;
import com.yowon.psc.case003.repro.domain.JoinHintReproReport;
import com.yowon.psc.case003.repro.infra.JoinHintReproJdbcRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JoinHintReproService {

    private final JoinHintReproJdbcRepository repository;

    public JoinHintReproService(JoinHintReproJdbcRepository repository) {
        this.repository = repository;
    }

    public JoinHintReproReport run(String serviceType, int limit) {
        // Warm-up to reduce first-query bias in local measurements.
        repository.findIdsWithoutHint(serviceType, limit);
        repository.findIdsWithHint(serviceType, limit);

        long withoutStartedAt = System.currentTimeMillis();
        List<Long> withoutHint = repository.findIdsWithoutHint(serviceType, limit);
        long withoutLatency = Math.max(1L, System.currentTimeMillis() - withoutStartedAt);

        long withStartedAt = System.currentTimeMillis();
        List<Long> withHint = repository.findIdsWithHint(serviceType, limit);
        long withLatency = Math.max(1L, System.currentTimeMillis() - withStartedAt);

        List<ExplainRow> withoutPlan = repository.explainWithoutHint(serviceType, limit);
        List<ExplainRow> withPlan = repository.explainWithHint(serviceType, limit);
        String withoutJson = repository.explainJsonWithoutHint(serviceType, limit);
        String withJson = repository.explainJsonWithHint(serviceType, limit);

        return new JoinHintReproReport(
            serviceType,
            limit,
            withoutLatency,
            withLatency,
            withoutHint.size(),
            withHint.size(),
            withoutHint,
            withHint,
            withoutPlan,
            withPlan,
            new ExplainJsonPair(withoutJson, withJson)
        );
    }
}
