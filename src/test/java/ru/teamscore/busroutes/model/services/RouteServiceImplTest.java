package ru.teamscore.busroutes.model.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import ru.teamscore.busroutes.data.entities.*;
import ru.teamscore.busroutes.data.repositories.RouteRepository;
import ru.teamscore.busroutes.data.repositories.StopRepository;
import ru.teamscore.busroutes.model.commands.BusinessHoursCommand;
import ru.teamscore.busroutes.model.commands.CreateRouteCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateRouteCommand;
import ru.teamscore.busroutes.model.commands.RouteStopCommand;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.exceptions.AlreadyExistsException;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.mapper.*;
import ru.teamscore.busroutes.model.models.*;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceImplTest {
    @Mock
    private StopRepository stopRepository;
    @Mock
    private RouteRepository routeRepository;
    private RouteService routeService;

    private static final String ROUTE_NAME_1 = "route1";
    private static final String ROUTE_NAME_2 = "route2";
    private static final String ROUTE_TYPE = "bus";
    private static final String STOP_NAME_1 = "Stop1";
    private static final String STOP_NAME_2 = "Stop2";
    private static final double LAT_1 = 53.198050;
    private static final double LON_1 = 50.108750;
    private static final double LAT_2 = 53.195873;
    private static final double LON_2 = 50.104954;
    private static final int ARRIVE_AT_STOP_1 = 0;
    private static final int ARRIVE_AT_STOP_2_ROUTE_1 = 120;
    private static final int ARRIVE_AT_STOP_2_ROUTE_2 = 60;
    private static final int STOP_ORDER_1 = 1;
    private static final int STOP_ORDER_2 = 2;
    private static final Duration INTERVAL_ROUTE_1 = Duration.ofMinutes(10);
    private static final Duration INTERVAL_ROUTE_2 = Duration.ofMinutes(15);
    private static final LocalTime START_TIME = LocalTime.of(5, 30);
    private static final LocalTime END_TIME = LocalTime.of(23, 0);

    private Stop stop1;
    private Stop stop2;
    private StopEntity stopEntity1;
    private StopEntity stopEntity2;
    private Route route1;
    private RouteEntity routeEntity1;
    private RouteEntity routeEntity2;

    @BeforeEach
    void setUp() {
        stop1 = Stop.valueOf(STOP_NAME_1, Stop.GeographicCoordinates.valueOf(LAT_1, LON_1));
        stop2 = Stop.valueOf(STOP_NAME_2, Stop.GeographicCoordinates.valueOf(LAT_2, LON_2));

        stopEntity1 = createStopEntity(STOP_NAME_1, LAT_1, LON_1);
        stopEntity2 = createStopEntity(STOP_NAME_2, LAT_2, LON_2);

        route1 = createRoute(ROUTE_NAME_1, INTERVAL_ROUTE_1, ARRIVE_AT_STOP_2_ROUTE_1);

        routeEntity1 = createRouteEntity(ROUTE_NAME_1, INTERVAL_ROUTE_1, ARRIVE_AT_STOP_2_ROUTE_1);
        routeEntity2 = createRouteEntity(ROUTE_NAME_2, INTERVAL_ROUTE_2, ARRIVE_AT_STOP_2_ROUTE_2);

        GeographicCoordinatesMapper geographicCoordinatesMapper =
            Mappers.getMapper(GeographicCoordinatesMapper.class);
        StopMapper stopMapper = Mappers.getMapper(StopMapper.class);
        RouteStopMapper routeStopMapper = Mappers.getMapper(RouteStopMapper.class);
        BusinessHoursMapper businessHoursMapper = Mappers.getMapper(BusinessHoursMapper.class);
        RouteMapper mapper = Mappers.getMapper(RouteMapper.class);

        ReflectionTestUtils.setField(stopMapper, "geographicCoordinatesMapper",
            geographicCoordinatesMapper);
        ReflectionTestUtils.setField(routeStopMapper, "stopMapper", stopMapper);
        ReflectionTestUtils.setField(mapper, "routeStopMapper", routeStopMapper);
        ReflectionTestUtils.setField(mapper, "businessHoursMapper", businessHoursMapper);

        Instant instant = Instant.parse("2026-01-18T12:35:00Z");
        ZoneId zoneId = ZoneId.of("Europe/Samara");

        routeService =
            new RouteServiceImpl(Clock.fixed(instant, zoneId), stopRepository, routeRepository,
                stopMapper, mapper);
    }

    @Nested
    class RouteAddition {
        @Test
        void addRoute_StopsExists_ReturnRoute() {
            CreateRouteCommand command = createRouteCommand();
            List<String> stopNames = List.of(STOP_NAME_1, STOP_NAME_2);

            when(routeRepository.existsByName(ROUTE_NAME_1)).thenReturn(false);
            when(stopRepository.findAllByNameIn(stopNames)).thenReturn(
                List.of(stopEntity1, stopEntity2));
            when(routeRepository.save(routeEntity1)).thenReturn(routeEntity1);

            Route addedRoute = routeService.addRoute(command);

            assertThat(addedRoute).isNotNull().isEqualTo(route1);
            verify(routeRepository).existsByName(ROUTE_NAME_1);
            verify(stopRepository).findAllByNameIn(stopNames);
            verify(routeRepository).save(routeEntity1);
        }

        @Test
        void addRoute_RouteAlreadyExists_ThrowException() {
            CreateRouteCommand command = createRouteCommand();
            when(routeRepository.existsByName(ROUTE_NAME_1)).thenReturn(true);

            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> routeService.addRoute(command));
        }

        @Test
        void addRoute_StopsNotExists_ThrowException() {
            CreateRouteCommand command = createRouteCommand();
            List<String> stopNames = List.of(STOP_NAME_1, STOP_NAME_2);

            when(routeRepository.existsByName(ROUTE_NAME_1)).thenReturn(false);
            when(stopRepository.findAllByNameIn(stopNames)).thenReturn(List.of(stopEntity1));

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> routeService.addRoute(command));
        }
    }

    @Nested
    class RouteRetrievalByStop {
        @Test
        void getRoutesByStop_TimeInRoute_ReturnSortedByTimeInRoute() {
            when(routeRepository.findRoutesByStop(STOP_NAME_1)).thenReturn(
                List.of(routeEntity1, routeEntity2));

            List<Travel> travels =
                routeService.getRoutesByStop(STOP_NAME_1, TravelSortOption.TIME_IN_ROUTE);

            assertThat(travels).hasSize(2);
            assertThat(travels.get(0).getTimeInRoute()).isLessThan(travels.get(1).getTimeInRoute());
        }

        @Test
        void getRoutesByStop_TimeInRoute_StopNotExists_ThrowException() {
            when(routeRepository.findRoutesByStop(STOP_NAME_1)).thenThrow(
                DataIntegrityViolationException.class);

            assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(
                () -> routeService.getRoutesByStop(STOP_NAME_1, TravelSortOption.TIME_IN_ROUTE));
        }

        @Test
        void getRoutesByStop_NearestArrival_ReturnSortedByNextArrival() {
            when(routeRepository.findRoutesByStop(STOP_NAME_1)).thenReturn(
                List.of(routeEntity1, routeEntity2));

            List<Travel> travels =
                routeService.getRoutesByStop(STOP_NAME_1, TravelSortOption.NEAREST_ARRIVAL);

            assertThat(travels).hasSize(2);
            assertThat(travels.get(0).getNextArrival()).isBefore(travels.get(1).getNextArrival());
        }

        @Test
        void getRoutesByStop_NearestArrival_StopNotExists_ThrowException() {
            when(routeRepository.findRoutesByStop(STOP_NAME_1)).thenThrow(
                DataIntegrityViolationException.class);

            assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(
                () -> routeService.getRoutesByStop(STOP_NAME_1, TravelSortOption.NEAREST_ARRIVAL));
        }
    }

    @Nested
    class RouteRetrievalByStops {
        @Test
        void getRoutesByStops_TimeInRoute_ReturnSortedByTimeInRoute() {
            when(routeRepository.findRoutesByBothStops(STOP_NAME_1, STOP_NAME_2)).thenReturn(
                List.of(routeEntity1, routeEntity2));

            List<Travel> travels = routeService.getRoutesByStops(STOP_NAME_1, STOP_NAME_2,
                TravelSortOption.TIME_IN_ROUTE);

            assertThat(travels).hasSize(2);
            assertThat(travels.get(0).getTimeInRoute()).isLessThan(travels.get(1).getTimeInRoute());
        }

        @Test
        void getRoutesByStops_NearestArrival_ReturnSortedByNextArrival() {
            when(routeRepository.findRoutesByBothStops(STOP_NAME_1, STOP_NAME_2)).thenReturn(
                List.of(routeEntity1, routeEntity2));

            List<Travel> travels = routeService.getRoutesByStops(STOP_NAME_1, STOP_NAME_2,
                TravelSortOption.NEAREST_ARRIVAL);

            assertThat(travels).hasSize(2);
            assertThat(travels.get(0).getNextArrival()).isBefore(travels.get(1).getNextArrival());
        }

        @Test
        void getRoutesByStops_NearestArrival_StopNotExists_ReturnSortedByNextArrival() {
            when(routeRepository.findRoutesByBothStops(STOP_NAME_1, STOP_NAME_2)).thenThrow(
                DataIntegrityViolationException.class);

            assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(
                () -> routeService.getRoutesByStops(STOP_NAME_1, STOP_NAME_2,
                    TravelSortOption.NEAREST_ARRIVAL));
        }
    }

    @Nested
    class RouteRetrievalByName {
        @Test
        void getRouteByName_ReturnRoute() {
            when(routeRepository.findByName(ROUTE_NAME_1)).thenReturn(Optional.of(routeEntity1));

            Route route = routeService.getRouteByName(ROUTE_NAME_1);

            assertThat(route).isNotNull().isEqualTo(route1);
        }

        @Test
        void getRouteByName_RouteNotExists_ThrowException() {
            when(routeRepository.findByName(ROUTE_NAME_1)).thenReturn(Optional.empty());

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> routeService.getRouteByName(ROUTE_NAME_1));
        }
    }

    @Nested
    class RouteCopying {
        @Test
        void copyRoute_ReverseOrder_ReturnRoute() {
            when(routeRepository.findByName(ROUTE_NAME_1)).thenReturn(Optional.of(routeEntity1));
            when(routeRepository.save(any(RouteEntity.class))).thenAnswer(i -> i.getArgument(0));

            Route route = routeService.copyRoute(ROUTE_NAME_1, true);

            RouteStop reverseStop1 = RouteStop.valueOf(ARRIVE_AT_STOP_1, STOP_ORDER_1, stop2);
            RouteStop reverseStop2 =
                RouteStop.valueOf(ARRIVE_AT_STOP_2_ROUTE_1, STOP_ORDER_2, stop1);

            assertThat(route).isNotNull().isNotEqualTo(route1);
            assertThat(route.getStops()).hasSize(2);
            assertThat(route.getStops()).element(0).isEqualTo(reverseStop1);
            assertThat(route.getStops()).element(1).isEqualTo(reverseStop2);
        }

        @Test
        void copyRoute_NotReverseOrder_ReturnRoute() {
            when(routeRepository.findByName(ROUTE_NAME_1)).thenReturn(Optional.of(routeEntity1));
            when(routeRepository.save(any(RouteEntity.class))).thenAnswer(i -> i.getArgument(0));

            Route route = routeService.copyRoute(ROUTE_NAME_1, false);

            assertThat(route.getStops()).isEqualTo(route1.getStops());
            assertThat(route.getBusinessHours()).isEqualTo(route1.getBusinessHours());
            assertThat(route.getInterval()).isEqualTo(route1.getInterval());
            assertThat(route.getType()).isEqualTo(route1.getType());
            assertThat(route.getName()).isNotEqualTo(route1.getName());
        }
    }

    @Nested
    class RouteUpdate {
        @Test
        void updateRouteByName_ReturnUpdatedRoute() {
            String updatedName = "route1_updated";
            Duration updatedInterval = Duration.ofMinutes(12);
            FullUpdateRouteCommand command =
                createFullUpdateRouteCommand(updatedName, updatedInterval);
            Route expectedRoute =
                createRoute(updatedName, updatedInterval, ARRIVE_AT_STOP_2_ROUTE_1);

            when(routeRepository.existsByName(updatedName)).thenReturn(false);
            when(routeRepository.findByName(ROUTE_NAME_1)).thenReturn(Optional.of(routeEntity1));
            when(stopRepository.findAllByNameIn(List.of(STOP_NAME_1, STOP_NAME_2))).thenReturn(
                List.of(stopEntity1, stopEntity2));
            when(routeRepository.save(any(RouteEntity.class))).thenAnswer(i -> i.getArgument(0));

            Route updatedRoute = routeService.updateRouteByName(ROUTE_NAME_1, command);

            assertThat(updatedRoute).isNotNull().isEqualTo(expectedRoute);
        }

        @Test
        void updateRouteByName_NewRouteAlreadyExists_ThrowException() {
            FullUpdateRouteCommand command =
                createFullUpdateRouteCommand("route1_updated", Duration.ofMinutes(12));
            when(routeRepository.existsByName(any(String.class))).thenReturn(true);

            assertThatExceptionOfType(AlreadyExistsException.class).isThrownBy(
                () -> routeService.updateRouteByName("oldRoute1", command));
        }

        @Test
        void updateRouteByName_StopNotFound_ThrowNotFoundException() {
            FullUpdateRouteCommand command =
                createFullUpdateRouteCommand("route1_updated", Duration.ofMinutes(12));
            when(routeRepository.existsByName("route1_updated")).thenReturn(false);

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> routeService.updateRouteByName(ROUTE_NAME_1, command));
        }

        @Test
        void updateRouteByName_NotFound_ThrowNotFoundException() {
            FullUpdateRouteCommand command =
                createFullUpdateRouteCommand("route1_updated", Duration.ofMinutes(12));
            when(routeRepository.existsByName(any(String.class))).thenReturn(false);

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> routeService.updateRouteByName("nonexistent", command));
        }
    }

    @Nested
    class RouteRemoval {
        @Test
        void removeRoute() {
            when(routeRepository.existsByName(ROUTE_NAME_1)).thenReturn(true);

            routeService.removeRoute(ROUTE_NAME_1);

            verify(routeRepository).existsByName(ROUTE_NAME_1);
        }

        @Test
        void removeRoute_RouteNotFound_ThrowNotFoundException() {
            when(routeRepository.existsByName(ROUTE_NAME_1)).thenReturn(false);

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> routeService.removeRoute(ROUTE_NAME_1));
        }
    }

    private CreateRouteCommand createRouteCommand() {
        return new CreateRouteCommand(RouteServiceImplTest.ROUTE_NAME_1, ROUTE_TYPE,
            RouteServiceImplTest.INTERVAL_ROUTE_1, new BusinessHoursCommand(START_TIME, END_TIME),
            List.of(new RouteStopCommand(ARRIVE_AT_STOP_1, STOP_ORDER_1, STOP_NAME_1),
                new RouteStopCommand(RouteServiceImplTest.ARRIVE_AT_STOP_2_ROUTE_1, STOP_ORDER_2,
                    STOP_NAME_2)));
    }

    private FullUpdateRouteCommand createFullUpdateRouteCommand(String name, Duration interval) {
        return new FullUpdateRouteCommand(name, ROUTE_TYPE, interval,
            new BusinessHoursCommand(START_TIME, END_TIME),
            List.of(new RouteStopCommand(ARRIVE_AT_STOP_1, STOP_ORDER_1, STOP_NAME_1),
                new RouteStopCommand(ARRIVE_AT_STOP_2_ROUTE_1, STOP_ORDER_2, STOP_NAME_2)));
    }

    private Route createRoute(String name, Duration interval, int arriveAtStop2) {
        List<RouteStop> stops = List.of(RouteStop.valueOf(ARRIVE_AT_STOP_1, STOP_ORDER_1, stop1),
            RouteStop.valueOf(arriveAtStop2, STOP_ORDER_2, stop2));
        BusinessHours businessHours = BusinessHours.valueOf(START_TIME, END_TIME);
        return Route.valueOf(name, ROUTE_TYPE, stops, interval, businessHours);
    }

    private RouteEntity createRouteEntity(String name, Duration interval, int arriveAtStop2) {
        return RouteEntity.builder().name(name).type(ROUTE_TYPE).interval(interval).businessHours(
            BusinessHoursEntity.builder().startAt(START_TIME).endAt(END_TIME).build()).stops(
            List.of(RouteStopEntity.builder().arriveAtFromStart(ARRIVE_AT_STOP_1)
                    .stopOrder(STOP_ORDER_1).stop(stopEntity1).build(),
                RouteStopEntity.builder().arriveAtFromStart(arriveAtStop2).stopOrder(STOP_ORDER_2)
                    .stop(stopEntity2).build())).build();
    }

    private StopEntity createStopEntity(String name, double lat, double lon) {
        return StopEntity.builder().name(name).geographicCoordinates(
            GeographicCoordinatesEntity.builder().latitude(lat).longitude(lon).build()).build();
    }
}
