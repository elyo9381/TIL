package com.yowon.psc.case002.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.yowon.psc.case002.reservation.application.dto.AdminReservListItem;
import com.yowon.psc.case002.reservation.application.dto.AdminReservSearchCondition;
import com.yowon.psc.case002.reservation.infra.mapper.AdminReservListRow;
import com.yowon.psc.case002.reservation.infra.mapper.ReservQueryMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminStyleReservQueryServiceTest {

    @Mock
    private ReservQueryMapper reservQueryMapper;

    @InjectMocks
    private AdminStyleReservQueryService service;

    @Test
    void searchShouldMapMapperRowsToApiItems() {
        AdminReservListRow row = new AdminReservListRow(
            100L,
            LocalDateTime.of(2026, 2, 28, 21, 30),
            "PAY_SUCCESS",
            "Night City Tour",
            "[{\"runo\":11,\"rsno\":101}]",
            new BigDecimal("23000")
        );

        when(reservQueryMapper.findReservList(org.mockito.ArgumentMatchers.any(AdminReservSearchCondition.class)))
            .thenReturn(List.of(row));

        List<AdminReservListItem> result = service.search(new AdminReservSearchCondition(10L, 0, 20, "ko"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRvno()).isEqualTo(100L);
        assertThat(result.get(0).getTicketName()).isEqualTo("Night City Tour");
        assertThat(result.get(0).getItemDTOsJson()).contains("runo");
        assertThat(result.get(0).getPayPrice()).isEqualByComparingTo("23000");
    }
}
