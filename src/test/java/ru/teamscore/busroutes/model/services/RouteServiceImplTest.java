package ru.teamscore.busroutes.model.services;

import org.junit.jupiter.api.BeforeEach;
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
    private RouteService routeService;
    @Mock
    private StopService stopService;
    @Mock
    private StopRepository stopRepository;
    @Mock
    private RouteRepository routeRepository;
    private StopMapper stopMapper;
    private RouteMapper mapper;
    private final Stop stop1 =
        Stop.valueOf("Stop1", Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));
    private final Stop stop2 =
        Stop.valueOf("Stop2", Stop.GeographicCoordinates.valueOf(53.195873, 50.104954));
    private StopEntity stopEntity1;
    private StopEntity stopEntity2;
    private Route route1;
    private Route route2;
    private RouteEntity routeEntity1;
    private RouteEntity routeEntity2;

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

        stopEntity1 = StopEntity.builder().name(stop1.getName()).geographicCoordinates(
            GeographicCoordinatesEntity.builder().latitude(stop1.getCoordinates().getLatitude())
                .longitude(stop1.getCoordinates().getLongitude()).build()).build();

        stopEntity2 = StopEntity.builder().name(stop2.getName()).geographicCoordinates(
            GeographicCoordinatesEntity.builder().latitude(stop2.getCoordinates().getLatitude())
                .longitude(stop2.getCoordinates().getLongitude()).build()).build();

        routeEntity1 = RouteEntity.builder().name(route1.getName()).type(route1.getType())
            .interval(route1.getInterval()).businessHours(
                BusinessHoursEntity.builder().startAt(route1.getBusinessHours().getStartAt())
                    .endAt(route1.getBusinessHours().getEndAt()).build()).stops(List.of(
                RouteStopEntity.builder().arriveAtFromStart(0).stopOrder(1).stop(stopEntity1)
                    .build(),
                RouteStopEntity.builder().arriveAtFromStart(120).stopOrder(2).stop(stopEntity2)
                    .build())).build();
        routeEntity2 = RouteEntity.builder().name(route2.getName()).type(route2.getType())
            .interval(route2.getInterval()).businessHours(
                BusinessHoursEntity.builder().startAt(route2.getBusinessHours().getStartAt())
                    .endAt(route2.getBusinessHours().getEndAt()).build()).stops(List.of(
                RouteStopEntity.builder().arriveAtFromStart(0).stopOrder(1).stop(stopEntity1)
                    .build(),
                RouteStopEntity.builder().arriveAtFromStart(120).stopOrder(2).stop(stopEntity2)
                    .build())).build();


        GeographicCoordinatesMapper geographicCoordinatesMapper =
            Mappers.getMapper(GeographicCoordinatesMapper.class);
        stopMapper = Mappers.getMapper(StopMapper.class);
        RouteStopMapper routeStopMapper = Mappers.getMapper(RouteStopMapper.class);
        BusinessHoursMapper businessHoursMapper = Mappers.getMapper(BusinessHoursMapper.class);
        mapper = Mappers.getMapper(RouteMapper.class);

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

    @Test
    void addRoute_StopsExists_ReturnRoute() {
        List<RouteStopCommand> routeStopCommands =
            List.of(new RouteStopCommand(0, 1, "Stop1"), new RouteStopCommand(120, 2, "Stop2"));
        CreateRouteCommand createRouteCommand =
            new CreateRouteCommand("route1", "bus", Duration.ofMinutes(10),
                new BusinessHoursCommand(LocalTime.of(5, 30), LocalTime.of(23, 0)),
                routeStopCommands);
        List<String> stopNames = List.of(stop1.getName(), stop2.getName());
        when(routeRepository.existsByName(route1.getName())).thenReturn(false);
        when(stopRepository.findAllByNameIn(stopNames)).thenReturn(
            List.of(stopEntity1, stopEntity2));
        when(routeRepository.save(routeEntity1)).thenReturn(routeEntity1);

        Route addedRoute = routeService.addRoute(createRouteCommand);

        assertThat(addedRoute).isNotNull().isEqualTo(route1);

        verify(routeRepository).existsByName(route1.getName());
        verify(stopRepository).findAllByNameIn(stopNames);
        verify(routeRepository).save(routeEntity1);
    }

    @Test
    void addRoute_RouteAlreadyExists_ThrowException() {
        List<RouteStopCommand> routeStopCommands =
            List.of(new RouteStopCommand(0, 1, "Stop1"), new RouteStopCommand(120, 2, "Stop2"));
        CreateRouteCommand createRouteCommand =
            new CreateRouteCommand("route1", "bus", Duration.ofMinutes(10),
                new BusinessHoursCommand(LocalTime.of(5, 30), LocalTime.of(23, 0)),
                routeStopCommands);
        when(routeRepository.existsByName(route1.getName())).thenReturn(true);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
            () -> routeService.addRoute(createRouteCommand));
    }

    @Test
    void addRoute_StopsNotExists_ThrowException() {
        List<RouteStopCommand> routeStopCommands =
            List.of(new RouteStopCommand(0, 1, "Stop1"), new RouteStopCommand(120, 2, "Stop2"));
        CreateRouteCommand createRouteCommand =
            new CreateRouteCommand("route1", "bus", Duration.ofMinutes(10),
                new BusinessHoursCommand(LocalTime.of(5, 30), LocalTime.of(23, 0)),
                routeStopCommands);
        List<String> stopNames = List.of(stop1.getName(), stop2.getName());
        when(routeRepository.existsByName(route1.getName())).thenReturn(false);
        when(stopRepository.findAllByNameIn(stopNames)).thenReturn(
            List.of(stopEntity1));

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.addRoute(createRouteCommand));
    }

    @Test
    void getRoutesByStop_TimeInRoute_ReturnSortedByTimeInRoute() {
        when(routeRepository.findRoutesByStop(stop1.getName())).thenReturn(
            List.of(routeEntity1, routeEntity2));

        List<Travel> travels =
            routeService.getRoutesByStop(stop1.getName(), TravelSortOption.TIME_IN_ROUTE);

        assertThat(travels).hasSize(2);
        assertThat(travels.get(0).getTimeInRoute()).isLessThan(travels.get(1).getTimeInRoute());
    }

    @Test
    void getRoutesByStop_TimeInRoute_StopNotExists_ThrowException() {
        String stop1Name = stop1.getName();
        when(routeRepository.findRoutesByStop(stop1Name)).thenThrow(
            DataIntegrityViolationException.class);

        assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(
            () -> routeService.getRoutesByStop(stop1Name, TravelSortOption.TIME_IN_ROUTE));
    }

    @Test
    void getRoutesByStop_NearestArrival_ReturnSortedByNextArrival() {
        when(routeRepository.findRoutesByStop(stop1.getName())).thenReturn(
            List.of(routeEntity1, routeEntity2));

        List<Travel> travels =
            routeService.getRoutesByStop(stop1.getName(), TravelSortOption.NEAREST_ARRIVAL);

        assertThat(travels).hasSize(2);
        assertThat(travels.get(0).getNextArrival()).isBefore(travels.get(1).getNextArrival());
    }

    @Test
    void getRoutesByStop_NearestArrival_StopNotExists_ThrowException() {
        String stop1Name = stop1.getName();
        when(routeRepository.findRoutesByStop(stop1Name)).thenThrow(
            DataIntegrityViolationException.class);

        assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(
            () -> routeService.getRoutesByStop(stop1Name, TravelSortOption.NEAREST_ARRIVAL));
    }

    @Test
    void getRoutesByStops_TimeInRoute_ReturnSortedByTimeInRoute() {
        when(routeRepository.findRoutesByBothStops(stop1.getName(), stop2.getName())).thenReturn(
            List.of(routeEntity1, routeEntity2));

        List<Travel> travels = routeService.getRoutesByStops(stop1.getName(), stop2.getName(),
            TravelSortOption.TIME_IN_ROUTE);

        assertThat(travels).hasSize(2);
        assertThat(travels.get(0).getTimeInRoute()).isLessThan(travels.get(1).getTimeInRoute());
    }

    @Test
    void getRoutesByStops_NearestArrival_ReturnSortedByNextArrival() {
        when(routeRepository.findRoutesByBothStops(stop1.getName(), stop2.getName())).thenReturn(
            List.of(routeEntity1, routeEntity2));

        List<Travel> travels = routeService.getRoutesByStops(stop1.getName(), stop2.getName(),
            TravelSortOption.NEAREST_ARRIVAL);

        assertThat(travels).hasSize(2);
        assertThat(travels.get(0).getNextArrival()).isBefore(travels.get(1).getNextArrival());
    }

    @Test
    void getRoutesByStops_NearestArrival_StopNotExists_ReturnSortedByNextArrival() {
        String stop1Name = stop1.getName();
        String stop2Name = stop2.getName();
        when(routeRepository.findRoutesByBothStops(stop1Name, stop2Name)).thenThrow(
            DataIntegrityViolationException.class);

        assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(
            () -> routeService.getRoutesByStops(stop1Name, stop2Name,
                TravelSortOption.NEAREST_ARRIVAL));
    }

    @Test
    void getRouteByName_ReturnRoute() {
        when(routeRepository.findByName(route1.getName())).thenReturn(Optional.of(routeEntity1));

        Route route = routeService.getRouteByName(route1.getName());

        assertThat(route).isNotNull().isEqualTo(route1);
    }

    @Test
    void getRouteByName_RouteNotExists_ThrowException() {
        String route1Name = route1.getName();
        when(routeRepository.findByName(route1Name)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.getRouteByName(route1Name));
    }

    @Test
    void copyRoute_ReverseOrder_ReturnRoute() {
        when(routeRepository.findByName(route1.getName())).thenReturn(Optional.of(routeEntity1));
        when(routeRepository.save(any(RouteEntity.class))).thenAnswer(i -> i.getArgument(0));

        Route route = routeService.copyRoute(this.route1.getName(), true);

        RouteStop reverseStop1 = RouteStop.valueOf(0, 1, stop2);
        RouteStop reverseStop2 = RouteStop.valueOf(120, 2, stop1);

        assertThat(route).isNotNull().isNotEqualTo(this.route1);
        assertThat(route.getStops()).hasSize(2);
        assertThat(route.getStops()).element(0).isEqualTo(reverseStop1);
        assertThat(route.getStops()).element(1).isEqualTo(reverseStop2);
    }

    @Test
    void copyRoute_NotReverseOrder_ReturnRoute() {
        when(routeRepository.findByName(route1.getName())).thenReturn(Optional.of(routeEntity1));
        when(routeRepository.save(any(RouteEntity.class))).thenAnswer(i -> i.getArgument(0));

        Route route = routeService.copyRoute(this.route1.getName(), false);

        assertThat(route.getStops()).isEqualTo(this.route1.getStops());
        assertThat(route.getBusinessHours()).isEqualTo(this.route1.getBusinessHours());
        assertThat(route.getInterval()).isEqualTo(this.route1.getInterval());
        assertThat(route.getType()).isEqualTo(this.route1.getType());
        assertThat(route.getName()).isNotEqualTo(this.route1.getName());
    }

    @Test
    void updateRouteByName_ReturnUpdatedRoute() {
        FullUpdateRouteCommand fullUpdateRouteCommand = new FullUpdateRouteCommand(
            "route1_updated", "bus", Duration.ofMinutes(12),
            new BusinessHoursCommand(LocalTime.of(5, 30), LocalTime.of(23, 0)),
            List.of(new RouteStopCommand(0, 1, "Stop1"), new RouteStopCommand(120, 2, "Stop2")));
        List<RouteStop> stopsRoute1 =
            List.of(RouteStop.valueOf(0, 1, stop1), RouteStop.valueOf(120, 2, stop2));
        BusinessHours businessHours =
            BusinessHours.valueOf(LocalTime.of(5, 30), LocalTime.of(23, 0));
        Route newRoute = Route.valueOf("route1_updated", "bus", stopsRoute1, Duration.ofMinutes(12),
            businessHours);

        when(routeRepository.existsByName(newRoute.getName())).thenReturn(false);
        when(routeRepository.findByName(route1.getName())).thenReturn(Optional.of(routeEntity1));
        when(stopRepository.findAllByNameIn(List.of("Stop1", "Stop2"))).thenReturn(
            List.of(stopEntity1, stopEntity2));
        when(routeRepository.save(any(RouteEntity.class))).thenAnswer(i -> i.getArgument(0));

        Route updatedRoute = routeService.updateRouteByName("route1", fullUpdateRouteCommand);

        assertThat(updatedRoute).isNotNull().isEqualTo(newRoute);
    }

    @Test
    void updateRouteByName_NewRouteAlreadyExists_ThrowException() {
        FullUpdateRouteCommand fullUpdateRouteCommand = new FullUpdateRouteCommand(
            "route1_updated", "bus", Duration.ofMinutes(12),
            new BusinessHoursCommand(LocalTime.of(5, 30), LocalTime.of(23, 0)),
            List.of(new RouteStopCommand(0, 1, "Stop1"), new RouteStopCommand(120, 2, "Stop2")));
        when(routeRepository.existsByName(any(String.class))).thenReturn(true);
        assertThatExceptionOfType(AlreadyExistsException.class).isThrownBy(
            () -> routeService.updateRouteByName("oldRoute1", fullUpdateRouteCommand));
    }

    @Test
    void updateRouteByName_StopNotFound_ThrowNotFoundException() {
        FullUpdateRouteCommand fullUpdateRouteCommand = new FullUpdateRouteCommand(
            "route1_updated", "bus", Duration.ofMinutes(12),
            new BusinessHoursCommand(LocalTime.of(5, 30), LocalTime.of(23, 0)),
            List.of(new RouteStopCommand(0, 1, "Stop1"), new RouteStopCommand(120, 2, "Stop2")));
        List<RouteStop> stopsRoute1 =
            List.of(RouteStop.valueOf(0, 1, stop1), RouteStop.valueOf(120, 2, stop2));
        BusinessHours businessHours =
            BusinessHours.valueOf(LocalTime.of(5, 30), LocalTime.of(23, 0));
        Route newRoute = Route.valueOf("route1_updated", "bus", stopsRoute1, Duration.ofMinutes(12),
            businessHours);

        when(routeRepository.existsByName(newRoute.getName())).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.updateRouteByName("route1", fullUpdateRouteCommand));
    }

    @Test
    void updateRouteByName_NotFound_ThrowNotFoundException() {
        FullUpdateRouteCommand fullUpdateRouteCommand = new FullUpdateRouteCommand(
            "route1_updated", "bus", Duration.ofMinutes(12),
            new BusinessHoursCommand(LocalTime.of(5, 30), LocalTime.of(23, 0)),
            List.of(new RouteStopCommand(0, 1, "Stop1"), new RouteStopCommand(120, 2, "Stop2")));
        when(routeRepository.existsByName(any(String.class))).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.updateRouteByName("nonexistent", fullUpdateRouteCommand));
    }

    @Test
    void removeRoute() {
        when(routeRepository.existsByName(route1.getName())).thenReturn(true);

        routeService.removeRoute(route1.getName());

        verify(routeRepository).existsByName(route1.getName());
    }

    @Test
    void removeRoute_RouteNotFound_ThrowNotFoundException() {
        String routeName = route1.getName();
        when(routeRepository.existsByName(routeName)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> routeService.removeRoute(routeName));
    }
}
