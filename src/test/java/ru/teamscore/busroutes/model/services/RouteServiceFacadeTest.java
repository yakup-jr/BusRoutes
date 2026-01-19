package ru.teamscore.busroutes.model.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceFacadeTest {
    private RouteServiceFacade facade;
    @Mock
    private RouteService routeService;
    @Mock
    private StopService stopService;

    @BeforeEach
    void setUp() {
        facade = new RouteServiceFacade(routeService, stopService);
    }

    @Test
    void removeStopWithCheck_Remove() {
        String stopName = "Stop1";
        when(routeService.isStopInUse(stopName)).thenReturn(false);

        facade.removeStopWithCheck(stopName);

        verify(stopService, times(1)).removeStopByName(stopName);
    }

    @Test
    void removeStopWithCheck_StopInUse_ThrowException() {
        String stopName = "Stop";
        when(routeService.isStopInUse(stopName)).thenReturn(true);

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> facade.removeStopWithCheck(stopName));

        verify(stopService, never()).removeStopByName(anyString());
    }
}
