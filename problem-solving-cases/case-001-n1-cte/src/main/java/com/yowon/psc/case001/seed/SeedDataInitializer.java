package com.yowon.psc.case001.seed;

import com.yowon.psc.case001.reservation.domain.PaymentEntity;
import com.yowon.psc.case001.reservation.domain.ReservationEntity;
import com.yowon.psc.case001.reservation.domain.ReservationItemEntity;
import com.yowon.psc.case001.reservation.domain.ReservationRepository;
import com.yowon.psc.case001.reservation.domain.ReservationTicketEntity;
import com.yowon.psc.case001.route.domain.RouteEntity;
import com.yowon.psc.case001.route.domain.RouteRepository;
import com.yowon.psc.case001.route.domain.RouteScheduleEntity;
import com.yowon.psc.case001.route.domain.RouteStationEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeedDataInitializer implements CommandLineRunner {

    private final RouteRepository routeRepository;
    private final ReservationRepository reservationRepository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.agno:10}")
    private long agno;

    @Value("${app.seed.route-count:20}")
    private int routeCount;

    @Value("${app.seed.reservation-count:2000}")
    private int reservationCount;

    @Value("${app.seed.random-seed:20260228}")
    private long randomSeed;

    public SeedDataInitializer(RouteRepository routeRepository, ReservationRepository reservationRepository) {
        this.routeRepository = routeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled || reservationRepository.countByAgno(agno) > 0) {
            return;
        }

        List<RouteEntity> routes = routeRepository.saveAll(buildRoutes(routeCount));
        reservationRepository.saveAll(buildReservations(routes, reservationCount, agno));
    }

    private List<RouteEntity> buildRoutes(int count) {
        List<RouteEntity> routes = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            RouteEntity route = new RouteEntity();
            route.setName("Route-" + i);

            for (int stationSeq = 1; stationSeq <= 10; stationSeq++) {
                RouteStationEntity station = new RouteStationEntity();
                station.setRoute(route);
                station.setSeq(stationSeq);
                station.setStationName("Station-" + i + "-" + stationSeq);
                route.getStations().add(station);
            }

            for (int j = 0; j < 3; j++) {
                RouteScheduleEntity schedule = new RouteScheduleEntity();
                schedule.setRoute(route);
                schedule.setDepartAt(LocalDateTime.now().plusHours(j));
                route.getSchedules().add(schedule);
            }

            routes.add(route);
        }

        return routes;
    }

    private List<ReservationEntity> buildReservations(List<RouteEntity> routes, int count, long targetAgno) {
        Random random = new Random(randomSeed);
        List<ReservationEntity> reservations = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ReservationEntity reservation = new ReservationEntity();
            reservation.setAgno(targetAgno);
            reservation.setCreatedAt(LocalDateTime.now().minusMinutes(i));

            int ticketCount = random.nextInt(2) + 1;
            for (int t = 0; t < ticketCount; t++) {
                ReservationTicketEntity ticket = new ReservationTicketEntity();
                ticket.setReservation(reservation);
                ticket.setRoute(routes.get(random.nextInt(routes.size())));
                reservation.getTickets().add(ticket);
            }

            int itemCount = random.nextInt(2) + 1;
            for (int j = 0; j < itemCount; j++) {
                ReservationItemEntity item = new ReservationItemEntity();
                item.setReservation(reservation);
                reservation.getItems().add(item);
            }

            int payCount = random.nextInt(2) + 1;
            for (int k = 0; k < payCount; k++) {
                PaymentEntity pay = new PaymentEntity();
                pay.setReservation(reservation);
                pay.setAmount(BigDecimal.valueOf(10000L + random.nextInt(5000)));
                reservation.getPays().add(pay);
            }

            reservations.add(reservation);
        }

        return reservations;
    }
}
