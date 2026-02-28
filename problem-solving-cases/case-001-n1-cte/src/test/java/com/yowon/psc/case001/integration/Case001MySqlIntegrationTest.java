package com.yowon.psc.case001.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.yowon.psc.case001.reservation.application.BaselineReservationService;
import com.yowon.psc.case001.reservation.application.OptimizedReservationService;
import com.yowon.psc.case001.reservation.application.ReservationCaseResponse;
import com.yowon.psc.case001.reservation.domain.ReservationRepository;
import com.yowon.psc.case001.route.domain.RouteRepository;
import com.yowon.psc.case001.support.MySqlIntegrationTestSupport;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
        "app.seed.enabled=true",
        "app.seed.agno=10",
        "app.seed.route-count=6",
        "app.seed.reservation-count=40"
    }
)
class Case001MySqlIntegrationTest extends MySqlIntegrationTestSupport {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BaselineReservationService baselineReservationService;

    @Autowired
    private OptimizedReservationService optimizedReservationService;

    @Test
    void seedDataShouldBeInsertedIntoRealMysql() {
        assertThat(routeRepository.count()).isEqualTo(6L);
        assertThat(reservationRepository.countByAgno(10L)).isEqualTo(40L);
    }

    @Test
    void baselineAndOptimizedShouldReturnSameCoreValues() {
        List<ReservationCaseResponse> baseline = baselineReservationService.load(10L, 20);
        List<ReservationCaseResponse> optimized = optimizedReservationService.load(10L, 20);

        assertThat(baseline).hasSize(20);
        assertThat(optimized).hasSize(20);

        Map<Long, ReservationCaseResponse> baselineById = baseline.stream()
            .collect(Collectors.toMap(ReservationCaseResponse::getReservationId, Function.identity()));
        Map<Long, ReservationCaseResponse> optimizedById = optimized.stream()
            .collect(Collectors.toMap(ReservationCaseResponse::getReservationId, Function.identity()));

        assertThat(optimizedById.keySet()).isEqualTo(baselineById.keySet());

        for (Long reservationId : baselineById.keySet()) {
            ReservationCaseResponse baselineRow = baselineById.get(reservationId);
            ReservationCaseResponse optimizedRow = optimizedById.get(reservationId);

            assertThat(optimizedRow.getCreatedAt()).isEqualTo(baselineRow.getCreatedAt());
            assertThat(optimizedRow.getRouteName()).isEqualTo(baselineRow.getRouteName());
            assertThat(optimizedRow.getFromStation()).isEqualTo(baselineRow.getFromStation());
            assertThat(optimizedRow.getToStation()).isEqualTo(baselineRow.getToStation());
            assertThat(optimizedRow.getTicketCount()).isEqualTo(baselineRow.getTicketCount());
            assertThat(optimizedRow.getItemCount()).isEqualTo(baselineRow.getItemCount());
            assertThat(optimizedRow.getPayCount()).isEqualTo(baselineRow.getPayCount());
            assertThat(optimizedRow.getTotalPaidAmount()).isEqualByComparingTo(baselineRow.getTotalPaidAmount());
        }
    }
}
