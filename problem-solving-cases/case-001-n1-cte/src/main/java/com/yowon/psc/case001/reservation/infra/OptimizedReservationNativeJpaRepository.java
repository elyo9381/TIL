package com.yowon.psc.case001.reservation.infra;

import com.yowon.psc.case001.reservation.domain.ReservationEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OptimizedReservationNativeJpaRepository
    extends JpaRepository<ReservationEntity, Long>, OptimizedReservationQueryRepository {

    @Override
    @Query(value =
        "WITH reserv_page AS ( " +
            "SELECT r.id, r.created_at " +
            "FROM tb_reserv r " +
            "WHERE r.agno = :agno " +
            "ORDER BY r.id DESC " +
            "LIMIT :limit " +
        "), " +
        "first_ticket AS ( " +
            "SELECT " +
                "t.reservation_id AS reservation_id, " +
                "MIN(t.id) AS first_ticket_id, " +
                "COUNT(*) AS ticket_count " +
            "FROM tb_reserv_ticket t " +
            "WHERE t.reservation_id IN (SELECT id FROM reserv_page) " +
            "GROUP BY t.reservation_id " +
        "), " +
        "ticket_agg AS ( " +
            "SELECT " +
                "ft.reservation_id AS reservation_id, " +
                "ft.ticket_count AS ticket_count, " +
                "rt.name AS route_name, " +
                "COALESCE(( " +
                    "SELECT rs.station_name " +
                    "FROM tb_route_station rs " +
                    "WHERE rs.route_id = rt.id " +
                    "ORDER BY rs.seq ASC " +
                    "LIMIT 1 " +
                "), '-') AS from_station, " +
                "COALESCE(( " +
                    "SELECT rs.station_name " +
                    "FROM tb_route_station rs " +
                    "WHERE rs.route_id = rt.id " +
                    "ORDER BY rs.seq DESC " +
                    "LIMIT 1 " +
                "), '-') AS to_station " +
            "FROM first_ticket ft " +
            "JOIN tb_reserv_ticket t ON t.id = ft.first_ticket_id " +
            "JOIN tb_route rt ON rt.id = t.route_id " +
        "), " +
        "item_agg AS ( " +
            "SELECT i.reservation_id AS reservation_id, COUNT(*) AS item_count " +
            "FROM tb_reserv_item i " +
            "WHERE i.reservation_id IN (SELECT id FROM reserv_page) " +
            "GROUP BY i.reservation_id " +
        "), " +
        "pay_agg AS ( " +
            "SELECT p.reservation_id AS reservation_id, " +
                   "COUNT(*) AS pay_count, " +
                   "COALESCE(SUM(p.amount), 0) AS total_paid_amount " +
            "FROM tb_pay p " +
            "WHERE p.reservation_id IN (SELECT id FROM reserv_page) " +
            "GROUP BY p.reservation_id " +
        ") " +
        "SELECT " +
            "rp.id AS reservationId, " +
            "rp.created_at AS createdAt, " +
            "COALESCE(ta.route_name, 'N/A') AS routeName, " +
            "COALESCE(ta.from_station, '-') AS fromStation, " +
            "COALESCE(ta.to_station, '-') AS toStation, " +
            "COALESCE(ta.ticket_count, 0) AS ticketCount, " +
            "COALESCE(ia.item_count, 0) AS itemCount, " +
            "COALESCE(pa.pay_count, 0) AS payCount, " +
            "COALESCE(pa.total_paid_amount, 0) AS totalPaidAmount " +
        "FROM reserv_page rp " +
        "LEFT JOIN ticket_agg ta ON ta.reservation_id = rp.id " +
        "LEFT JOIN item_agg ia ON ia.reservation_id = rp.id " +
        "LEFT JOIN pay_agg pa ON pa.reservation_id = rp.id " +
        "ORDER BY rp.id DESC",
        nativeQuery = true)
    List<OptimizedReservationRow> findByAgno(@Param("agno") Long agno, @Param("limit") int limit);
}
