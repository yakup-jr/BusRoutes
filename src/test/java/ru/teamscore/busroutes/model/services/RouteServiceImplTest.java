package ru.teamscore.busroutes.model.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.teamscore.busroutes.data.entities.*;
import ru.teamscore.busroutes.data.repositories.RouteRepository;
import ru.teamscore.busroutes.data.repositories.StopRepository;
import ru.teamscore.busroutes.model.commands.BusinessHoursCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateRouteCommand;
import ru.teamscore.busroutes.model.commands.RouteStopCommand;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.exceptions.AlreadyExistsException;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.mapper.*;
import ru.teamscore.busroutes.model.models.*;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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


    private static final UUID ROUTE_ID1 = UUID.randomUUID();
    private static final String ROUTE_NAME_1 = "route1";
    private static final String ROUTE_NAME_2 = "route2";
    private static final String NOT_EXISTING_NAME = "NotExistingName";
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
        stop1 = Stop.builder().name(STOP_NAME_1)
            .coordinates(Stop.GeographicCoordinates.valueOf(LAT_1, LON_1)).build();
        stop2 = Stop.builder().name(STOP_NAME_2)
            .coordinates(Stop.GeographicCoordinates.valueOf(LAT_2, LON_2)).build();

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
                mapper);
    }

    @Nested
    class RouteRetrievalByStop {
        @Test
        void getRoutesByStop_TimeInRoute_ReturnSortedByTimeInRoute() {
            when(routeRepository.findRoutesByStop(STOP_NAME_1)).thenReturn(
                List.of(routeEntity1, routeEntity2));
            when(stopRepository.existsByName(STOP_NAME_1)).thenReturn(true);

            List<Travel> travels =
                routeService.getRoutesByStop(STOP_NAME_1, TravelSortOption.TIME_IN_ROUTE);

            assertThat(travels).hasSize(2);
            assertThat(travels.get(0).getTimeInRoute()).isLessThan(travels.get(1).getTimeInRoute());
        }

        @Test
        void getRoutesByStop_TimeInRoute_StopNotExists_ThrowException() {
            when(stopRepository.existsByName(NOT_EXISTING_NAME)).thenReturn(false);

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> routeService.getRoutesByStop(NOT_EXISTING_NAME,
                    TravelSortOption.TIME_IN_ROUTE));
        }

        @Test
        void getRoutesByStop_NearestArrival_ReturnSortedByNextArrival() {
            when(routeRepository.findRoutesByStop(STOP_NAME_1)).thenReturn(
                List.of(routeEntity1, routeEntity2));
            when(stopRepository.existsByName(STOP_NAME_1)).thenReturn(true);

            List<Travel> travels =
                routeService.getRoutesByStop(STOP_NAME_1, TravelSortOption.NEAREST_ARRIVAL);

            assertThat(travels).hasSize(2);
            assertThat(travels.get(0).getNextArrival()).isBefore(travels.get(1).getNextArrival());
        }

        @Test
        void getRoutesByStop_NearestArrival_StopNotExists_ThrowException() {
            when(stopRepository.existsByName(NOT_EXISTING_NAME)).thenReturn(false);

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> routeService.getRoutesByStop(NOT_EXISTING_NAME,
                    TravelSortOption.NEAREST_ARRIVAL));
        }
    }

    @Nested
    class RouteRetrievalByStops {
        @Test
        void getRoutesByStops_TimeInRoute_ReturnSortedByTimeInRoute() {
            when(routeRepository.findRoutesByBothStops(STOP_NAME_1, STOP_NAME_2)).thenReturn(
                List.of(routeEntity1, routeEntity2));
            when(stopRepository.existsByName(STOP_NAME_1)).thenReturn(true);
            when(stopRepository.existsByName(STOP_NAME_2)).thenReturn(true);

            List<Travel> travels = routeService.getRoutesByStops(STOP_NAME_1, STOP_NAME_2,
                TravelSortOption.TIME_IN_ROUTE);

            assertThat(travels).hasSize(2);
            assertThat(travels.get(0).getTimeInRoute()).isLessThan(travels.get(1).getTimeInRoute());
        }

        @Test
        void getRoutesByStops_NearestArrival_ReturnSortedByNextArrival() {
            when(routeRepository.findRoutesByBothStops(STOP_NAME_1, STOP_NAME_2)).thenReturn(
                List.of(routeEntity1, routeEntity2));
            when(stopRepository.existsByName(STOP_NAME_1)).thenReturn(true);
            when(stopRepository.existsByName(STOP_NAME_2)).thenReturn(true);

            List<Travel> travels = routeService.getRoutesByStops(STOP_NAME_1, STOP_NAME_2,
                TravelSortOption.NEAREST_ARRIVAL);

            assertThat(travels).hasSize(2);
            assertThat(travels.get(0).getNextArrival()).isBefore(travels.get(1).getNextArrival());
        }

        @Test
        void getRoutesByStops_NearestArrival_StopNotExists_ThrowException() {
            when(stopRepository.existsByName(NOT_EXISTING_NAME)).thenReturn(false);

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> routeService.getRoutesByStops(NOT_EXISTING_NAME, STOP_NAME_2,
                    TravelSortOption.NEAREST_ARRIVAL));
        }
    }

    @Nested
    class RouteRetrievalByName {
        @Test
        void getRouteByName_ReturnRoute() {
            when(routeRepository.findByNameWithStops(ROUTE_NAME_1)).thenReturn(
                List.of(routeEntity1));

            Iterable<Route> route = routeService.getRouteByName(ROUTE_NAME_1);

            assertThat(route).isNotNull().hasSize(1).element(0).isEqualTo(route1);
        }

        @Test
        void getRouteByName_RouteNotExists_ThrowException() {
            when(routeRepository.findByNameWithStops(NOT_EXISTING_NAME)).thenReturn(List.of());

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> routeService.getRouteByName(NOT_EXISTING_NAME));
        }
    }

    @Nested
    class RouteCopying {
        @Test
        void copyRoute_ReverseOrder_ReturnRoute() {
            when(routeRepository.findById(routeEntity1.getId())).thenReturn(
                Optional.of(routeEntity1));
            when(routeRepository.save(any(RouteEntity.class))).thenAnswer(i -> i.getArgument(0));

            Route route = routeService.copyRoute(routeEntity1.getId(), true);

            RouteStop reverseStop1 =
                RouteStop.builder().arriveAtFromStart(ARRIVE_AT_STOP_1).stopOrder(STOP_ORDER_1)
                    .stop(stop2).build();
            RouteStop reverseStop2 = RouteStop.builder().arriveAtFromStart(ARRIVE_AT_STOP_2_ROUTE_1)
                .stopOrder(STOP_ORDER_2).stop(stop1).build();

            assertThat(route).isNotNull().isNotEqualTo(route1);
            assertThat(route.getStops()).hasSize(2);
            assertThat(route.getStops()).element(0).isEqualTo(reverseStop1);
            assertThat(route.getStops()).element(1).isEqualTo(reverseStop2);
        }

        @Test
        void copyRoute_NotReverseOrder_ReturnRoute() {
            when(routeRepository.findById(routeEntity1.getId())).thenReturn(
                Optional.of(routeEntity1));
            when(routeRepository.save(any(RouteEntity.class))).thenAnswer(i -> i.getArgument(0));

            Route route = routeService.copyRoute(routeEntity1.getId(), false);

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
        void updateRoute_ReturnUpdatedRoute() {
            String updatedName = "route1_updated";
            Duration updatedInterval = Duration.ofMinutes(12);
            FullUpdateRouteCommand command =
                createFullUpdateRouteCommand(updatedName, updatedInterval);
            Route expectedRoute =
                createRoute(updatedName, updatedInterval, ARRIVE_AT_STOP_2_ROUTE_1);

            when(routeRepository.findById(routeEntity1.getId())).thenReturn(
                Optional.of(routeEntity1));
            when(routeRepository.existsByName(updatedName)).thenReturn(false);
            when(stopRepository.findAllByNameIn(List.of(STOP_NAME_1, STOP_NAME_2))).thenReturn(
                List.of(stopEntity1, stopEntity2));

            Route updatedRoute = routeService.updateRoute(routeEntity1.getId(), command);

            assertThat(updatedRoute).isNotNull().isEqualTo(expectedRoute);
        }

        @Test
        void updateRoute_NewRouteAlreadyExists_ThrowException() {
            FullUpdateRouteCommand command =
                createFullUpdateRouteCommand("route1_updated", Duration.ofMinutes(12));
            when(routeRepository.findById(routeEntity1.getId())).thenReturn(
                Optional.of(routeEntity1));
            when(routeRepository.existsByName(command.name())).thenReturn(true);

            assertThatExceptionOfType(AlreadyExistsException.class).isThrownBy(
                () -> routeService.updateRoute(routeEntity1.getId(), command));
        }

        @Test
        void updateRoute_StopNotFound_ThrowNotFoundException() {
            FullUpdateRouteCommand command =
                createFullUpdateRouteCommand("route1_updated", Duration.ofMinutes(12));

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> routeService.updateRoute(routeEntity1.getId(), command));
        }

        @Test
        void updateRoute_NotFound_ThrowNotFoundException() {
            FullUpdateRouteCommand command =
                createFullUpdateRouteCommand("route1_updated", Duration.ofMinutes(12));

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> routeService.updateRoute(UUID.randomUUID(), command));
        }
    }

    @Nested
    class RouteRemoval {
        @Test
        void deleteRoute() {
            when(routeRepository.existsByName(ROUTE_NAME_1)).thenReturn(true);

            routeService.deleteRouteByName(ROUTE_NAME_1);

            verify(routeRepository).existsByName(ROUTE_NAME_1);
        }

        @Test
        void deleteRoute_RouteNotFound_ThrowNotFoundException() {
            when(routeRepository.existsByName(ROUTE_NAME_1)).thenReturn(false);

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> routeService.deleteRouteByName(ROUTE_NAME_1));
        }
    }

    private FullUpdateRouteCommand createFullUpdateRouteCommand(String name, Duration interval) {
        return new FullUpdateRouteCommand(name, ROUTE_TYPE, interval,
            new BusinessHoursCommand(START_TIME, END_TIME),
            List.of(new RouteStopCommand(ARRIVE_AT_STOP_1, STOP_ORDER_1, STOP_NAME_1),
                new RouteStopCommand(ARRIVE_AT_STOP_2_ROUTE_1, STOP_ORDER_2, STOP_NAME_2)));
    }

    private Route createRoute(String name, Duration interval, int arriveAtStop2) {
        List<RouteStop> stops = List.of(
            RouteStop.builder()
                .arriveAtFromStart(ARRIVE_AT_STOP_1)
                .stopOrder(STOP_ORDER_1)
                .stop(stop1).build(),
            RouteStop.builder()
                .arriveAtFromStart(arriveAtStop2)
                .stopOrder(STOP_ORDER_2)
                .stop(stop2)
                .build());
        BusinessHours businessHours =
            BusinessHours.builder()
                .startAt(START_TIME)
                .endAt(END_TIME).build();
        return Route.builder()
            .id(ROUTE_ID1)
            .name(name)
            .type(ROUTE_TYPE)
            .stops(stops)
            .interval(interval)
            .businessHours(businessHours).build();
    }

    private RouteEntity createRouteEntity(String name, Duration interval, int arriveAtStop2) {
        return RouteEntity.builder()
            .id(ROUTE_ID1)
            .name(name)
            .type(ROUTE_TYPE)
            .interval(interval)
            .businessHours(BusinessHoursEntity.builder()
                .startAt(START_TIME)
                .endAt(END_TIME).build())
            .stops(new ArrayList<>(
                List.of(RouteStopEntity.builder()
                        .arriveAtFromStart(ARRIVE_AT_STOP_1)
                        .stopOrder(STOP_ORDER_1)
                        .stop(stopEntity1).build(),
                    RouteStopEntity.builder()
                        .arriveAtFromStart(arriveAtStop2)
                        .stopOrder(STOP_ORDER_2)
                        .stop(stopEntity2).build()))).build();
    }

    private StopEntity createStopEntity(String name, double lat, double lon) {
        return StopEntity.builder().name(name)
            .geographicCoordinates(
                GeographicCoordinatesEntity.builder()
                    .latitude(lat)
                    .longitude(lon).build())
            .build();
    }
}
