package ru.teamscore.busroutes.model.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.models.*;

import java.time.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceImplTest {
    private RouteService routeService;
    @Mock
    private StopService stopService;
    private final Stop stop1 =
        Stop.valueOf("Stop1", Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));
    private final Stop stop2 =
        Stop.valueOf("Stop2", Stop.GeographicCoordinates.valueOf(53.195873, 50.104954));
    private Route route1;
    private Route route2;

    @BeforeEach
    void setUp() {
        List<RouteStop> stopsRoute1 = List.of(RouteStop.valueOf(0, 1, stop1.getName()),
            RouteStop.valueOf(120, 2, stop2.getName()));

        List<RouteStop> stopsRoute2 = List.of(RouteStop.valueOf(0, 1, stop1.getName()),
            RouteStop.valueOf(60, 2, stop2.getName()));

        BusinessHours businessHours =
            BusinessHours.valueOf(LocalTime.of(5, 30), LocalTime.of(23, 0));


        route1 = Route.valueOf("route1", "bus", stopsRoute1, Duration.ofMinutes(10), businessHours);
        route2 = Route.valueOf("route2", "bus", stopsRoute2, Duration.ofMinutes(15), businessHours);

        CopyOnWriteArrayList<Route> routes = new CopyOnWriteArrayList<>();
        routes.add(route1);
        routes.add(route2);
        Instant instant = Instant.parse("2026-01-18T12:35:00Z");
        ZoneId zoneId = ZoneId.of("Europe/Samara");

        routeService =
            Mockito.spy(new RouteServiceImpl(Clock.fixed(instant, zoneId), routes, stopService));
    }

    @Test
    void addRoute_StopsExists_ReturnNewRoute() {
        Stop stop3 =
            Stop.valueOf("Stop3", Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));
        Stop stop4 =
            Stop.valueOf("Stop4", Stop.GeographicCoordinates.valueOf(53.195873, 50.104954));

        when(stopService.containsStop("Stop3")).thenReturn(true);
        when(stopService.containsStop("Stop4")).thenReturn(true);


        List<RouteStop> stops = List.of(RouteStop.valueOf(0, 1, stop3.getName()),
            RouteStop.valueOf(120, 2, stop4.getName()));

        BusinessHours businessHours =
            BusinessHours.valueOf(LocalTime.of(6, 30), LocalTime.of(0, 0));
        Route newRoute =
            Route.valueOf("route3", "bus", stops, Duration.ofSeconds(120), businessHours);

        Route addedRoute = routeService.addRoute(newRoute);

        assertThat(addedRoute).isNotNull().isEqualTo(newRoute);
        verify(routeService, times(1)).addRoute(newRoute);
    }

    @Test
    void addRoute_RouteAlreadyExists_ThrowIllegalArgumentException() {
        Stop stop3 =
            Stop.valueOf("Stop3", Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));
        Stop stop4 =
            Stop.valueOf("Stop4", Stop.GeographicCoordinates.valueOf(53.195873, 50.104954));

        when(stopService.containsStop("Stop3")).thenReturn(true);
        when(stopService.containsStop("Stop4")).thenReturn(true);


        List<RouteStop> stops = List.of(RouteStop.valueOf(0, 1, stop3.getName()),
            RouteStop.valueOf(120, 2, stop4.getName()));

        BusinessHours businessHours =
            BusinessHours.valueOf(LocalTime.of(6, 30), LocalTime.of(0, 0));
        Route newRoute =
            Route.valueOf("route3", "bus", stops, Duration.ofSeconds(120), businessHours);

        routeService.addRoute(newRoute);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
            () -> routeService.addRoute(newRoute));
    }

    @Test
    void addRoute_StopsNotExists_ThrowNotFoundException() {
        Stop stop3 =
            Stop.valueOf("Stop3", Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));
        Stop stop4 =
            Stop.valueOf("Stop4", Stop.GeographicCoordinates.valueOf(53.195873, 50.104954));

        when(stopService.containsStop("Stop3")).thenReturn(false);

        List<RouteStop> stops = List.of(RouteStop.valueOf(0, 1, stop3.getName()),
            RouteStop.valueOf(120, 2, stop4.getName()));

        BusinessHours businessHours =
            BusinessHours.valueOf(LocalTime.of(6, 30), LocalTime.of(0, 0));
        Route newRoute =
            Route.valueOf("route3", "bus", stops, Duration.ofSeconds(120), businessHours);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.addRoute(newRoute));
    }

    @Test
    void getRoutesByStop_TimeInRoute_ReturnSortedByTimeInRoute() {
        when(stopService.containsStop("Stop1")).thenReturn(true);

        List<Travel> travels =
            routeService.getRoutesByStop("Stop1", TravelSortOption.TIME_IN_ROUTE);

        assertThat(travels).hasSize(2);
        assertThat(travels.get(0).getTimeInRoute()).isLessThan(travels.get(1).getTimeInRoute());
    }

    @Test
    void getRoutesByStop_TimeInRoute_StopNotExists_ThrowNotFoundException() {
        when(stopService.containsStop("Stop1")).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.getRoutesByStop("Stop1", TravelSortOption.TIME_IN_ROUTE));
    }

    @Test
    void getRoutesByStop_NearestArrival_ReturnSortedByNextArrival() {
        when(stopService.containsStop("Stop1")).thenReturn(true);

        List<Travel> travels =
            routeService.getRoutesByStop("Stop1", TravelSortOption.NEAREST_ARRIVAL);

        assertThat(travels).hasSize(2);
        assertThat(travels.get(0).getNextArrival()).isBefore(travels.get(1).getNextArrival());
    }

    @Test
    void getRoutesByStop_NearestArrival_StopNotExists_ThrowNotFoundException() {
        when(stopService.containsStop("Stop1")).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.getRoutesByStop("Stop1", TravelSortOption.NEAREST_ARRIVAL));
    }

    @Test
    void getRoutesByStops_TimeInRoute_ReturnSortedByTimeInRoute() {
        when(stopService.containsStop("Stop1")).thenReturn(true);
        when(stopService.containsStop("Stop2")).thenReturn(true);

        List<Travel> travels =
            routeService.getRoutesByStops("Stop1", "Stop2", TravelSortOption.TIME_IN_ROUTE);

        assertThat(travels).hasSize(2);
        assertThat(travels.get(0).getTimeInRoute()).isLessThan(travels.get(1).getTimeInRoute());
    }

    @Test
    void getRoutesByStops_NearestArrival_ReturnSortedByNextArrival() {
        when(stopService.containsStop("Stop1")).thenReturn(true);
        when(stopService.containsStop("Stop2")).thenReturn(true);


        List<Travel> travels =
            routeService.getRoutesByStops("Stop1", "Stop2", TravelSortOption.NEAREST_ARRIVAL);

        assertThat(travels).hasSize(2);
        assertThat(travels.get(0).getNextArrival()).isBefore(travels.get(1).getNextArrival());
    }

    @Test
    void getRoutesByStops_NearestArrival_StopNotExists_ReturnSortedByNextArrival() {
        when(stopService.containsStop("Stop1")).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.getRoutesByStops("Stop1", "Stop2",
                TravelSortOption.NEAREST_ARRIVAL));
    }

    @Test
    void isStopInUse_ReturnTrue() {
        when(stopService.containsStop("Stop1")).thenReturn(true);

        boolean isStop1InUse = routeService.isStopInUse("Stop1");

        assertThat(isStop1InUse).isTrue();
    }

    @Test
    void isStopInUse_ReturnFalse() {
        when(stopService.containsStop("Stop3")).thenReturn(true);

        boolean isStop1InUse = routeService.isStopInUse("Stop3");

        assertThat(isStop1InUse).isFalse();
    }

    @Test
    void isStopInUse_StopNotExists_ThrowNotFoundException() {
        when(stopService.containsStop("Stop1")).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.isStopInUse("Stop1"));
    }

    @Test
    void getRouteByName_ReturnRoute() {
        when(stopService.containsStop("Stop1")).thenReturn(true);
        when(stopService.containsStop("Stop2")).thenReturn(true);

        Route route = routeService.getRouteByName("route1");

        assertThat(route).isNotNull().isEqualTo(route1);
    }

    @Test
    void getRouteByName_StopNotExists_ReturnRoute() {
        when(stopService.containsStop("Stop1")).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.getRouteByName("route1"));
    }

    @Test
    void getRouteByName_NotFound_ThrowException() {
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.getRouteByName("nonexistent"));
    }

    @Test
    void copyRoute_ReverseOrder_ReturnRoute() {
        when(stopService.containsStop("Stop1")).thenReturn(true);
        when(stopService.containsStop("Stop2")).thenReturn(true);

        Route route = routeService.copyRoute(route1.getName(), true);

        RouteStop reverseStop1 = RouteStop.valueOf(0, 1, "Stop2");
        RouteStop reverseStop2 = RouteStop.valueOf(120, 2, "Stop1");


        assertThat(route).isNotNull().isNotEqualTo(route1);
        assertThat(route.getStops()).hasSize(2);
        assertThat(route.getStops()).element(0).isEqualTo(reverseStop1);
        assertThat(route.getStops()).element(1).isEqualTo(reverseStop2);
    }

    @Test
    void copyRoute_ReverseOrder_StopNotExists_ThrowNotFoundException() {
        when(stopService.containsStop("Stop1")).thenReturn(false);

        String name = route1.getName();
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.copyRoute(name, true));
    }

    @Test
    void copyRoute_NotReverseOrder_ReturnRoute() {
        when(stopService.containsStop("Stop1")).thenReturn(true);
        when(stopService.containsStop("Stop2")).thenReturn(true);

        Route route = routeService.copyRoute(route1.getName(), false);

        assertThat(route.getStops()).isEqualTo(route1.getStops());
        assertThat(route.getBusinessHours()).isEqualTo(route1.getBusinessHours());
        assertThat(route.getInterval()).isEqualTo(route1.getInterval());
        assertThat(route.getType()).isEqualTo(route1.getType());
        assertThat(route.getName()).isNotEqualTo(route1.getName());
    }

    @Test
    void copyRoute_NotReverseOrder_StopNotExists_ThrowNotFoundException() {
        when(stopService.containsStop("Stop1")).thenReturn(false);

        String name = route1.getName();
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.copyRoute(name, false));
    }

    @Test
    void updateRouteByName_ReturnUpdatedRoute() {
        when(stopService.containsStop("Stop1")).thenReturn(true);
        when(stopService.containsStop("Stop2")).thenReturn(true);

        List<RouteStop> stopsRoute1 = List.of(RouteStop.valueOf(0, 1, stop1.getName()),
            RouteStop.valueOf(120, 2, stop2.getName()));

        BusinessHours businessHours =
            BusinessHours.valueOf(LocalTime.of(5, 30), LocalTime.of(23, 0));
        Route newRoute = Route.valueOf("route1_updated", "bus", stopsRoute1, Duration.ofMinutes(12),
            businessHours);

        Route updatedRoute = routeService.updateRouteByName("route1", newRoute);

        assertThat(updatedRoute).isNotNull().isEqualTo(newRoute);
    }

    @Test
    void updateRouteByName_NewRouteAlreadyExists_ThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
            () -> routeService.updateRouteByName("route1", route1));
    }

    @Test
    void updateRouteByName_StopNotFound_ThrowNotFoundException() {
        when(stopService.containsStop("Stop1")).thenReturn(false);

        List<RouteStop> stopsRoute1 = List.of(RouteStop.valueOf(0, 1, stop1.getName()),
            RouteStop.valueOf(120, 2, stop2.getName()));

        BusinessHours businessHours =
            BusinessHours.valueOf(LocalTime.of(5, 30), LocalTime.of(23, 0));
        Route newRoute = Route.valueOf("route1_updated", "bus", stopsRoute1, Duration.ofMinutes(12),
            businessHours);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.updateRouteByName("route1", newRoute));
    }

    @Test
    void updateRouteByName_NotFound_ThrowNotFoundException() {
        Route newRoute = Route.valueOf("new", "bus",
            List.of(RouteStop.valueOf(0, 1, stop1.getName()),
                RouteStop.valueOf(120, 2, stop2.getName())), Duration.ofMinutes(12),
            BusinessHours.valueOf(LocalTime.of(5, 30), LocalTime.of(23, 0)));

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.updateRouteByName("nonexistent", newRoute));
    }

    @Test
    void removeRoute() {
        when(stopService.containsStop("Stop1")).thenReturn(true);
        when(stopService.containsStop("Stop2")).thenReturn(true);

        routeService.removeRoute("route1");

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.getRouteByName("route1"));
        verify(routeService, times(1)).removeRoute("route1");
    }

    @Test
    void removeRoute_StopNotExists_ThrowNotFoundException() {
        when(stopService.containsStop("Stop1")).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.removeRoute("route1"));
    }

    @Test
    void removeRoute_NotFound_ThrowNotFoundException() {
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.removeRoute("nonexistent"));
    }
}
