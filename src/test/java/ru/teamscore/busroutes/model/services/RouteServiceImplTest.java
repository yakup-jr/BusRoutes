package ru.teamscore.busroutes.model.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.models.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RouteServiceImplTest {
    private RouteService routeService;
    private final Stop stop1 =
        Stop.valueOf("Stop1", Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));
    private final Stop stop2 =
        Stop.valueOf("Stop2", Stop.GeographicCoordinates.valueOf(53.195873, 50.104954));
    private Route route1;
    private Route route2;

    @BeforeEach
    void setUp() {
        List<RouteStop> stopsRoute1 =
            List.of(RouteStop.valueOf(0, 1, stop1), RouteStop.valueOf(120, 2, stop2));

        List<RouteStop> stopsRoute2 =
            List.of(RouteStop.valueOf(0, 1, stop1), RouteStop.valueOf(60, 2, stop2));

        BusinessHours businessHours =
            BusinessHours.valueOf(LocalTime.of(5, 30), LocalTime.of(23, 0));

        route1 = Route.valueOf("route1", "bus", stopsRoute1, Duration.ofMinutes(10), businessHours);
        route2 = Route.valueOf("route2", "bus", stopsRoute2, Duration.ofMinutes(15), businessHours);

        List<Route> routes = new ArrayList<>();
        routes.add(route1);
        routes.add(route2);

        routeService = Mockito.spy(new RouteServiceImpl(routes));
    }

    @Test
    void addRoute_ReturnNewRoute() {
        Stop stop3 =
            Stop.valueOf("Stop3", Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));
        Stop stop4 =
            Stop.valueOf("Stop4", Stop.GeographicCoordinates.valueOf(53.195873, 50.104954));

        List<RouteStop> stops =
            List.of(RouteStop.valueOf(0, 1, stop3), RouteStop.valueOf(120, 2, stop4));

        BusinessHours businessHours =
            BusinessHours.valueOf(LocalTime.of(6, 30), LocalTime.of(0, 0));
        Route newRoute =
            Route.valueOf("route3", "bus", stops, Duration.ofSeconds(120), businessHours);

        Route addedRoute = routeService.addRoute(newRoute);

        assertThat(addedRoute).isNotNull().isEqualTo(newRoute);
        verify(routeService, times(1)).addRoute(newRoute);
    }

    @Test
    void getRoutesByStop_TimeInRoute_ReturnSortedByTimeInRoute() {
        List<Travel> travels =
            routeService.getRoutesByStop("Stop1", TravelSortOption.TIME_IN_ROUTE);

        assertThat(travels).hasSize(2);
        assertThat(travels.get(0).getTimeInRoute()).isLessThan(travels.get(1).getTimeInRoute());
    }

    @Test
    void getRoutesByStop_NearestArrival_ReturnSortedByNextArrival() {
        List<Travel> travels =
            routeService.getRoutesByStop("Stop1", TravelSortOption.NEAREST_ARRIVAL);

        assertThat(travels).hasSize(2);
        assertThat(travels.get(0).getNextArrival()).isBefore(travels.get(1).getNextArrival());
    }

    @Test
    void getRoutesByStops_TimeInRoute_ReturnSortedByTimeInRoute() {
        List<Travel> travels =
            routeService.getRoutesByStops("Stop1", "Stop2", TravelSortOption.TIME_IN_ROUTE);

        assertThat(travels).hasSize(2);
        assertThat(travels.get(0).getTimeInRoute()).isLessThan(travels.get(1).getTimeInRoute());
    }

    @Test
    void getRoutesByStops_NearestArrival_ReturnSortedByNextArrival() {
        List<Travel> travels =
            routeService.getRoutesByStops("Stop1", "Stop2", TravelSortOption.NEAREST_ARRIVAL);

        assertThat(travels).hasSize(2);
        assertThat(travels.get(0).getNextArrival()).isBefore(travels.get(1).getNextArrival());
    }

    @Test
    void getRouteByName_ReturnRoute() {
        Route route = routeService.getRouteByName("route1");

        assertThat(route).isNotNull().isEqualTo(route1);
    }

    @Test
    void getRouteByName_NotFound_ThrowException() {
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.getRouteByName("nonexistent"));
    }

    @Test
    void copyRoute_ReverseOrder_ReturnRoute() {
        Route route = routeService.copyRoute(route1, true);

        assertThat(route).isNotNull().isNotEqualTo(route1);
        assertThat(route.getStops()).hasSize(2);
        assertThat(route.getStops().get(0).getStop()).isEqualTo(stop2);
        assertThat(route.getStops().get(1).getStop()).isEqualTo(stop1);
    }

    @Test
    void copyRoute_NotReverseOrder_ReturnRoute() {
        Route route = routeService.copyRoute(route1, false);

        assertThat(route).isNotNull().isEqualTo(route1);
    }

    @Test
    void updateRouteByName_ReturnUpdatedRoute() {
        List<RouteStop> stopsRoute1 =
            List.of(RouteStop.valueOf(0, 1, stop1), RouteStop.valueOf(120, 2, stop2));

        BusinessHours businessHours =
            BusinessHours.valueOf(LocalTime.of(5, 30), LocalTime.of(23, 0));
        Route newRoute = Route.valueOf("route1_updated", "bus", stopsRoute1, Duration.ofMinutes(12),
            businessHours);

        Route updatedRoute = routeService.updateRouteByName("route1", newRoute);

        assertThat(updatedRoute).isNotNull().isEqualTo(newRoute);
    }

    @Test
    void updateRouteByName_NotFound_ThrowException() {
        Route newRoute = Route.valueOf("new", "bus",
            List.of(RouteStop.valueOf(0, 1, stop1), RouteStop.valueOf(120, 2, stop2)),
            Duration.ofMinutes(12),
            BusinessHours.valueOf(LocalTime.of(5, 30), LocalTime.of(23, 0)));

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.updateRouteByName("nonexistent", newRoute));
    }

    @Test
    void removeRoute() {
        routeService.removeRoute("route1");

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.getRouteByName("route1"));
        verify(routeService, times(1)).removeRoute("route1");
    }

    @Test
    void removeRoute_NotFound_ThrowException() {
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.removeRoute("nonexistent"));
    }
}
