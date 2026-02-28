package com.yowon.psc.case001.reservation.application;

import com.yowon.psc.case001.reservation.domain.PaymentEntity;
import com.yowon.psc.case001.reservation.domain.ReservationEntity;
import com.yowon.psc.case001.reservation.domain.ReservationTicketEntity;
import com.yowon.psc.case001.reservation.infra.OptimizedReservationRow;
import com.yowon.psc.case001.route.domain.RouteEntity;
import com.yowon.psc.case001.route.domain.RouteStationEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

public class ReservationCaseResponse {

    private final Long reservationId;
    private final LocalDateTime createdAt;
    private final String routeName;
    private final String fromStation;
    private final String toStation;
    private final int ticketCount;
    private final int itemCount;
    private final int payCount;
    private final BigDecimal totalPaidAmount;

    public ReservationCaseResponse(
        Long reservationId,
        LocalDateTime createdAt,
        String routeName,
        String fromStation,
        String toStation,
        int ticketCount,
        int itemCount,
        int payCount,
        BigDecimal totalPaidAmount
    ) {
        this.reservationId = reservationId;
        this.createdAt = createdAt;
        this.routeName = routeName;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.ticketCount = ticketCount;
        this.itemCount = itemCount;
        this.payCount = payCount;
        this.totalPaidAmount = totalPaidAmount;
    }

    public static ReservationCaseResponse fromEntity(ReservationEntity reservation) {
        RouteEntity route = reservation.getTickets().stream()
            .findFirst()
            .map(ReservationTicketEntity::getRoute)
            .orElse(null);

        String routeNameValue = route == null ? "N/A" : route.getName();
        String from = extractStation(route, true);
        String to = extractStation(route, false);

        BigDecimal totalPayAmount = reservation.getPays().stream()
            .map(PaymentEntity::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReservationCaseResponse(
            reservation.getId(),
            reservation.getCreatedAt(),
            routeNameValue,
            from,
            to,
            reservation.getTickets().size(),
            reservation.getItems().size(),
            reservation.getPays().size(),
            totalPayAmount
        );
    }

    public static ReservationCaseResponse fromProjection(OptimizedReservationRow row) {
        return new ReservationCaseResponse(
            row.getReservationId(),
            row.getCreatedAt(),
            row.getRouteName(),
            row.getFromStation(),
            row.getToStation(),
            row.getTicketCount(),
            row.getItemCount(),
            row.getPayCount(),
            row.getTotalPaidAmount()
        );
    }

    private static String extractStation(RouteEntity route, boolean start) {
        if (route == null || route.getStations().isEmpty()) {
            return "-";
        }

        Comparator<RouteStationEntity> comparator = Comparator.comparingInt(RouteStationEntity::getSeq);
        Optional<RouteStationEntity> station = start
            ? route.getStations().stream().min(comparator)
            : route.getStations().stream().max(comparator);
        return station.map(RouteStationEntity::getStationName).orElse("-");
    }

    public Long getReservationId() {
        return reservationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getFromStation() {
        return fromStation;
    }

    public String getToStation() {
        return toStation;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public int getItemCount() {
        return itemCount;
    }

    public int getPayCount() {
        return payCount;
    }

    public BigDecimal getTotalPaidAmount() {
        return totalPaidAmount;
    }
}
