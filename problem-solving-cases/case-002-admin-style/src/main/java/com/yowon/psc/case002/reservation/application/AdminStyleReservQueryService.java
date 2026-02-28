package com.yowon.psc.case002.reservation.application;

import com.yowon.psc.case002.reservation.application.dto.AdminReservListItem;
import com.yowon.psc.case002.reservation.application.dto.AdminReservSearchCondition;
import com.yowon.psc.case002.reservation.infra.mapper.AdminReservListRow;
import com.yowon.psc.case002.reservation.infra.mapper.ReservQueryMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AdminStyleReservQueryService {

    private final ReservQueryMapper reservQueryMapper;

    public AdminStyleReservQueryService(ReservQueryMapper reservQueryMapper) {
        this.reservQueryMapper = reservQueryMapper;
    }

    public List<AdminReservListItem> search(AdminReservSearchCondition condition) {
        List<AdminReservListRow> rows = reservQueryMapper.findReservList(condition);
        return rows.stream()
            .map(row -> new AdminReservListItem(
                row.getRvno(),
                row.getRegdt(),
                row.getStatus(),
                row.getTicketName(),
                row.getItemDTOsJson(),
                row.getPayPrice()
            ))
            .collect(Collectors.toList());
    }
}
