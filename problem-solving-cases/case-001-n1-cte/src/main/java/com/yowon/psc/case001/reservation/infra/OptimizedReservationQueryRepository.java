package com.yowon.psc.case001.reservation.infra;

import java.util.List;

public interface OptimizedReservationQueryRepository {

    List<OptimizedReservationRow> findByAgno(Long agno, int limit);
}
