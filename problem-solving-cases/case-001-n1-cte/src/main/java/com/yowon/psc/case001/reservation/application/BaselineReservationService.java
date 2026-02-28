package com.yowon.psc.case001.reservation.application;

import com.yowon.psc.case001.reservation.domain.ReservationEntity;
import com.yowon.psc.case001.reservation.domain.ReservationRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BaselineReservationService {

    private final ReservationRepository reservationRepository;
 
    public BaselineReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationCaseResponse> load(Long agno, int limit) {
        List<ReservationEntity> reservations = reservationRepository.findByAgnoOrderByIdDesc(
            agno,
            PageRequest.of(0, Math.max(limit, 1))
        );

        List<ReservationCaseResponse> response = reservations.stream()
            .map(ReservationCaseResponse::fromEntity)
            .collect(Collectors.toList());

        return response;
    }
}
