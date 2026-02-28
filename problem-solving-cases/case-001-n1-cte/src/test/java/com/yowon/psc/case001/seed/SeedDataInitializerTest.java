package com.yowon.psc.case001.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yowon.psc.case001.reservation.domain.ReservationEntity;
import com.yowon.psc.case001.reservation.domain.ReservationRepository;
import com.yowon.psc.case001.route.domain.RouteEntity;
import com.yowon.psc.case001.route.domain.RouteRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SeedDataInitializerTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private SeedDataInitializer seedDataInitializer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(seedDataInitializer, "seedEnabled", true);
        ReflectionTestUtils.setField(seedDataInitializer, "agno", 10L);
        ReflectionTestUtils.setField(seedDataInitializer, "routeCount", 20);
        ReflectionTestUtils.setField(seedDataInitializer, "reservationCount", 2000);
        ReflectionTestUtils.setField(seedDataInitializer, "randomSeed", 20260228L);
    }

    @Test
    void runShouldCreateConfiguredRouteAndReservationSizeWhenEmpty() throws Exception {
        when(reservationRepository.countByAgno(10L)).thenReturn(0L);

        when(routeRepository.saveAll(org.mockito.ArgumentMatchers.<RouteEntity>anyList())).thenAnswer(invocation -> {
            List<RouteEntity> routes = invocation.getArgument(0);
            long id = 1L;
            for (RouteEntity route : routes) {
                route.setId(id++);
            }
            return routes;
        });

        when(reservationRepository.saveAll(org.mockito.ArgumentMatchers.<ReservationEntity>anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        seedDataInitializer.run();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RouteEntity>> routeCaptor = ArgumentCaptor.forClass(List.class);
        verify(routeRepository).saveAll(routeCaptor.capture());
        assertThat(routeCaptor.getValue()).hasSize(20);
        assertThat(routeCaptor.getValue().get(0).getStations()).hasSize(10);
        assertThat(routeCaptor.getValue().get(0).getSchedules()).hasSize(3);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReservationEntity>> reservationCaptor = ArgumentCaptor.forClass(List.class);
        verify(reservationRepository).saveAll(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue()).hasSize(2000);
    }

    @Test
    void runShouldSkipWhenReservationAlreadyExists() throws Exception {
        when(reservationRepository.countByAgno(anyLong())).thenReturn(1L);

        seedDataInitializer.run();

        verify(routeRepository, never()).saveAll(org.mockito.ArgumentMatchers.<RouteEntity>anyList());
        verify(reservationRepository, never()).saveAll(org.mockito.ArgumentMatchers.<ReservationEntity>anyList());
    }
}
