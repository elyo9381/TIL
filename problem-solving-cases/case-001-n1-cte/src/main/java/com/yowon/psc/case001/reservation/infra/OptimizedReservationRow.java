package com.yowon.psc.case001.reservation.infra;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OptimizedReservationRow {

    Long getReservationId();

    LocalDateTime getCreatedAt();

    String getRouteName();

    String getFromStation();

    String getToStation();

    int getTicketCount();

    int getItemCount();

    int getPayCount();

    BigDecimal getTotalPaidAmount();
}
