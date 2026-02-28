package com.yowon.psc.case003.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.yowon.psc.case003.repro.application.JoinHintReproService;
import com.yowon.psc.case003.repro.domain.ExplainRow;
import com.yowon.psc.case003.repro.domain.JoinHintReproReport;
import com.yowon.psc.case003.support.MySqlIntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "app.schema.auto-create=true",
    "app.seed.enabled=true",
    "app.seed.reservation-count=30000"
})
class Case003JoinHintReproIntegrationTest extends MySqlIntegrationTestSupport {

    @Autowired
    private JoinHintReproService service;

    @Test
    void explainShouldShowDifferentJoinEntryPointWhenHintIsApplied() {
        JoinHintReproReport report = service.run("VIP", 20);

        List<ExplainRow> withoutHintPlan = report.getWithoutHintPlan();
        List<ExplainRow> withHintPlan = report.getWithHintPlan();

        assertThat(withoutHintPlan).isNotEmpty();
        assertThat(withHintPlan).isNotEmpty();

        ExplainRow withoutFirst = withoutHintPlan.get(0);
        ExplainRow withFirst = withHintPlan.get(0);

        assertThat(withoutFirst.getTableName()).isEqualTo("tr");
        assertThat(withFirst.getTableName()).isEqualTo("r");
        assertThat(withoutFirst.getExtra()).contains("Using temporary");
        assertThat(withoutFirst.getExtra()).contains("Using filesort");
        assertThat(withFirst.getExtra()).contains("Backward index scan");
    }

    @Test
    void reportShouldContainBothPlansAndLatency() {
        JoinHintReproReport report = service.run("VIP", 20);

        assertThat(report.getWithoutHintPlan()).isNotEmpty();
        assertThat(report.getWithHintPlan()).isNotEmpty();
        assertThat(report.getWithoutHintLatencyMs()).isPositive();
        assertThat(report.getWithHintLatencyMs()).isPositive();
        assertThat(report.getWithoutHintResultSize()).isEqualTo(20);
        assertThat(report.getWithHintResultSize()).isEqualTo(20);
        assertThat(report.getWithoutHintExplainJson()).isNotBlank();
        assertThat(report.getWithHintExplainJson()).isNotBlank();
        assertThat(report.getWithoutHintExplainJson()).contains("\"table_name\": \"tr\"");
        assertThat(report.getWithHintExplainJson()).contains("\"table_name\": \"r\"");
    }

    @Test
    void hintOffAndHintOnShouldReturnSameReservationIdsForSameFilter() {
        JoinHintReproReport report = service.run("VIP", 20);

        assertThat(report.getWithoutHintSampleIds()).hasSize(20);
        assertThat(report.getWithHintSampleIds()).hasSize(20);
        assertThat(report.getWithHintSampleIds())
            .containsExactlyElementsOf(report.getWithoutHintSampleIds());
    }
}
