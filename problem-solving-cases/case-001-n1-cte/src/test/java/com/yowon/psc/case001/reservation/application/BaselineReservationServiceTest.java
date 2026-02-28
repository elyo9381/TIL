package com.yowon.psc.case001.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.yowon.psc.case001.reservation.domain.PaymentEntity;
import com.yowon.psc.case001.reservation.domain.ReservationEntity;
import com.yowon.psc.case001.reservation.domain.ReservationItemEntity;
import com.yowon.psc.case001.reservation.domain.ReservationRepository;
import com.yowon.psc.case001.reservation.domain.ReservationTicketEntity;
import com.yowon.psc.case001.route.domain.RouteEntity;
import com.yowon.psc.case001.route.domain.RouteStationEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BaselineReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private BaselineReservationService baselineReservationService;

    @Test
    void loadShouldMapEntityGraphToResponse() {
        ReservationEntity reservation = new ReservationEntity();
        reservation.setId(100L);
        reservation.setAgno(10L);
        reservation.setCreatedAt(LocalDateTime.of(2026, 2, 28, 20, 30));

        RouteEntity route = new RouteEntity();
        route.setId(1L);
        route.setName("A-B Express");

        RouteStationEntity start = new RouteStationEntity();
        start.setSeq(1);
        start.setStationName("Seoul");
        start.setRoute(route);
        route.getStations().add(start);

        RouteStationEntity end = new RouteStationEntity();
        end.setSeq(10);
        end.setStationName("Busan");
        end.setRoute(route);
        route.getStations().add(end);

        ReservationTicketEntity ticket1 = new ReservationTicketEntity();
        ticket1.setReservation(reservation);
        ticket1.setRoute(route);

        ReservationTicketEntity ticket2 = new ReservationTicketEntity();
        ticket2.setReservation(reservation);
        ticket2.setRoute(route);

        ReservationItemEntity item = new ReservationItemEntity();
        item.setReservation(reservation);

        PaymentEntity pay1 = new PaymentEntity();
        pay1.setReservation(reservation);
        pay1.setAmount(new BigDecimal("12000"));

        PaymentEntity pay2 = new PaymentEntity();
        pay2.setReservation(reservation);
        pay2.setAmount(new BigDecimal("8000"));

        reservation.getTickets().add(ticket1);
        reservation.getTickets().add(ticket2);
        reservation.getItems().add(item);
        reservation.getPays().add(pay1);
        reservation.getPays().add(pay2);

        when(reservationRepository.findByAgnoOrderByIdDesc(eq(10L), any())).thenReturn(List.of(reservation));

        List<ReservationCaseResponse> result = baselineReservationService.load(10L, 20);

        assertThat(result).hasSize(1);
        ReservationCaseResponse row = result.get(0);
        assertThat(row.getReservationId()).isEqualTo(100L);
        assertThat(row.getRouteName()).isEqualTo("A-B Express");
        assertThat(row.getFromStation()).isEqualTo("Seoul");
        assertThat(row.getToStation()).isEqualTo("Busan");
        assertThat(row.getTicketCount()).isEqualTo(2);
        assertThat(row.getItemCount()).isEqualTo(1);
        assertThat(row.getPayCount()).isEqualTo(2);
        assertThat(row.getTotalPaidAmount()).isEqualByComparingTo("20000");
    }
}
