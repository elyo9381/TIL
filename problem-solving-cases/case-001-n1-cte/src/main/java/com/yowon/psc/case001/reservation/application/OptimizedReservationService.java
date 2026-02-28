package com.yowon.psc.case001.reservation.application;

import com.yowon.psc.case001.reservation.infra.OptimizedReservationQueryRepository;
import com.yowon.psc.case001.reservation.infra.OptimizedReservationRow;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OptimizedReservationService {

    private final OptimizedReservationQueryRepository queryRepository;
 
    public OptimizedReservationService(OptimizedReservationQueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    public List<ReservationCaseResponse> load(Long agno, int limit) {
        List<OptimizedReservationRow> rows = queryRepository.findByAgno(agno, limit);
        List<ReservationCaseResponse> response = rows.stream()
            .map(ReservationCaseResponse::fromProjection)
            .collect(Collectors.toList());

        return response;
    }
}
