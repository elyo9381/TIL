package com.yowon.psc.case001.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.yowon.psc.case001.reservation.infra.OptimizedReservationQueryRepository;
import com.yowon.psc.case001.reservation.infra.OptimizedReservationRow;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OptimizedReservationServiceTest {

    @Mock
    private OptimizedReservationQueryRepository queryRepository;

    @InjectMocks
    private OptimizedReservationService optimizedReservationService;

    @Test
    void loadShouldMapProjectionToResponse() {
        OptimizedReservationRow row = new OptimizedReservationRow() {
            @Override
            public Long getReservationId() {
                return 100L;
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return LocalDateTime.of(2026, 2, 28, 20, 30);
            }

            @Override
            public String getRouteName() {
                return "A-B Express";
            }

            @Override
            public String getFromStation() {
                return "Seoul";
            }

            @Override
            public String getToStation() {
                return "Busan";
            }

            @Override
            public int getTicketCount() {
                return 2;
            }

            @Override
            public int getItemCount() {
                return 1;
            }

            @Override
            public int getPayCount() {
                return 2;
            }

            @Override
            public BigDecimal getTotalPaidAmount() {
                return new BigDecimal("20000");
            }
        };

        when(queryRepository.findByAgno(10L, 20)).thenReturn(List.of(row));

        List<ReservationCaseResponse> result = optimizedReservationService.load(10L, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReservationId()).isEqualTo(100L);
        assertThat(result.get(0).getRouteName()).isEqualTo("A-B Express");
        assertThat(result.get(0).getTotalPaidAmount()).isEqualByComparingTo("20000");
    }
}
