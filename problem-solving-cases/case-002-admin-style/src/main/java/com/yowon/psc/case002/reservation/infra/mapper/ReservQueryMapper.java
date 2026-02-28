package com.yowon.psc.case002.reservation.infra.mapper;

import com.yowon.psc.case002.reservation.application.dto.AdminReservSearchCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservQueryMapper {

    List<AdminReservListRow> findReservList(AdminReservSearchCondition condition);
}
