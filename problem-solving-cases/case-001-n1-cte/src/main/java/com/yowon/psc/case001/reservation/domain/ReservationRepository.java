package com.yowon.psc.case001.reservation.domain;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    List<ReservationEntity> findByAgnoOrderByIdDesc(Long agno, Pageable pageable);

    long countByAgno(Long agno);
}
