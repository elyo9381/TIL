package com.yowon.psc.case002.reservation.api;

import com.yowon.psc.case002.reservation.application.AdminStyleReservQueryService;
import com.yowon.psc.case002.reservation.application.dto.AdminReservListItem;
import com.yowon.psc.case002.reservation.application.dto.AdminReservSearchCondition;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/case-002/reservations")
public class Case002AdminReservController {

    private final AdminStyleReservQueryService queryService;

    public Case002AdminReservController(AdminStyleReservQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/optimized")
    public List<AdminReservListItem> optimized(
        @RequestParam("agno") Long agno,
        @RequestParam(name = "offset", defaultValue = "0") int offset,
        @RequestParam(name = "limit", defaultValue = "20") int limit,
        @RequestParam(name = "locale", defaultValue = "ko") String locale
    ) {
        AdminReservSearchCondition condition = new AdminReservSearchCondition(
            agno,
            Math.max(0, offset),
            Math.max(1, limit),
            locale
        );
        return queryService.search(condition);
    }
}
