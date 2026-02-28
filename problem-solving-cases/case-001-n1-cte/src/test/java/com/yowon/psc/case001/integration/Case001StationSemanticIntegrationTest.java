package com.yowon.psc.case001.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.yowon.psc.case001.reservation.application.BaselineReservationService;
import com.yowon.psc.case001.reservation.application.OptimizedReservationService;
import com.yowon.psc.case001.reservation.application.ReservationCaseResponse;
import com.yowon.psc.case001.reservation.domain.PaymentEntity;
import com.yowon.psc.case001.reservation.domain.ReservationEntity;
import com.yowon.psc.case001.reservation.domain.ReservationRepository;
import com.yowon.psc.case001.reservation.domain.ReservationTicketEntity;
import com.yowon.psc.case001.route.domain.RouteEntity;
import com.yowon.psc.case001.route.domain.RouteRepository;
import com.yowon.psc.case001.route.domain.RouteStationEntity;
import com.yowon.psc.case001.support.MySqlIntegrationTestSupport;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.seed.enabled=false")
class Case001StationSemanticIntegrationTest extends MySqlIntegrationTestSupport {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BaselineReservationService baselineReservationService;

    @Autowired
    private OptimizedReservationService optimizedReservationService;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        routeRepository.deleteAll();

        RouteEntity route = new RouteEntity();
        route.setName("Semantic-Route");

        RouteStationEntity stationStart = new RouteStationEntity();
        stationStart.setRoute(route);
        stationStart.setSeq(3);
        stationStart.setStationName("Station-A");
        route.getStations().add(stationStart);

        RouteStationEntity stationEnd = new RouteStationEntity();
        stationEnd.setRoute(route);
        stationEnd.setSeq(7);
        stationEnd.setStationName("Station-Z");
        route.getStations().add(stationEnd);

        RouteEntity savedRoute = routeRepository.save(route);

        ReservationEntity reservation = new ReservationEntity();
        reservation.setAgno(77L);
        reservation.setCreatedAt(LocalDateTime.of(2026, 2, 28, 15, 0));

        ReservationTicketEntity ticket = new ReservationTicketEntity();
        ticket.setReservation(reservation);
        ticket.setRoute(savedRoute);
        reservation.getTickets().add(ticket);

        PaymentEntity pay = new PaymentEntity();
        pay.setReservation(reservation);
        pay.setAmount(new BigDecimal("12000"));
        reservation.getPays().add(pay);

        reservationRepository.save(reservation);
    }

    @Test
    void optimizedShouldMatchBaselineForDynamicStationSequence() {
        List<ReservationCaseResponse> baseline = baselineReservationService.load(77L, 20);
        List<ReservationCaseResponse> optimized = optimizedReservationService.load(77L, 20);

        assertThat(baseline).hasSize(1);
        assertThat(optimized).hasSize(1);
        assertThat(optimized.get(0).getRouteName()).isEqualTo(baseline.get(0).getRouteName());
        assertThat(optimized.get(0).getFromStation()).isEqualTo(baseline.get(0).getFromStation());
        assertThat(optimized.get(0).getToStation()).isEqualTo(baseline.get(0).getToStation());
    }
}
