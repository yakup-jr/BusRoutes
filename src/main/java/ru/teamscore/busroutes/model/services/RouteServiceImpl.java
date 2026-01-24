package ru.teamscore.busroutes.model.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.teamscore.busroutes.data.entities.RouteEntity;
import ru.teamscore.busroutes.data.entities.StopEntity;
import ru.teamscore.busroutes.data.repositories.RouteRepository;
import ru.teamscore.busroutes.data.repositories.StopRepository;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.mapper.RouteMapper;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.RouteStop;
import ru.teamscore.busroutes.model.models.Travel;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class RouteServiceImpl implements RouteService {
    private final Clock clock;
    private final CopyOnWriteArrayList<Route> oldRoutes;
    private final StopService stopService;
    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final RouteMapper mapper;

    @Override
    @Transactional
    public Route addRoute(Route route) {
        if (routeRepository.existsByName(route.getName())) {
            throw new IllegalArgumentException("Route already exists");
        }

        List<String> stopNames =
            StreamSupport.stream(route.getStops().spliterator(), false)
                .map(routeStop -> routeStop.getStop().getName()).toList();
        List<StopEntity> fetchedStops = stopRepository.findAllByNameIn(stopNames);
        if (fetchedStops.size() != stopNames.size()) {
            throw new NotFoundException("some stops not exists", ItemType.STOP);
        }

        RouteEntity mappedEntity = mapper.map(route);
        RouteEntity savedEntity = routeRepository.save(mappedEntity);
        return mapper.map(savedEntity);
    }

    private void isStopExists(String stopName) {
        if (!stopService.containsStop(stopName)) {
            throw new NotFoundException(String.format("Stop with name %s not found", stopName),
                ItemType.STOP);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Travel> getRoutesByStop(String stopName, TravelSortOption sort) {
        List<Route> routes = mapper.map(routeRepository.findRoutesByStop(stopName));

        List<Travel> travels = routes.stream().map(route -> createTravel(route, stopName)).toList();

        return sortTravels(travels, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Travel> getRoutesByStops(String fromStopName, String toStopName,
                                         TravelSortOption sort) {
        List<Route> routes =
            mapper.map(routeRepository.findRoutesByBothStops(fromStopName, toStopName));
        List<Travel> travels =
            routes.stream().map(route -> createTravel(route, fromStopName)).toList();

        return sortTravels(travels, sort);
    }

    @Override
    public boolean isStopInUse(String stopName) {
        isStopExists(stopName);
        return oldRoutes.stream().anyMatch(route -> route.containsStop(stopName));
    }

    private Travel createTravel(Route route, String stopName) {
        RouteStop routeStop = StreamSupport.stream(route.getStops().spliterator(), false)
            .filter(rs -> rs.getStop().getName().equals(stopName)).findFirst()
            .orElseThrow(() -> new NotFoundException(stopName, ItemType.STOP));

        Duration timeInRoute =
            route.getInterval().minus(Duration.ofSeconds(routeStop.getArriveAtFromStart()));
        LocalTime startTime = route.getBusinessHours().getStartAt();
        LocalTime now = LocalTime.now(clock);

        LocalTime arrivalTime;
        if (now.isBefore(startTime) || now.isAfter(route.getBusinessHours().getEndAt())) {
            arrivalTime = startTime;
        } else {
            long secondsSinceStart = Duration.between(startTime, now).getSeconds();
            long intervalSeconds = route.getInterval().getSeconds();
            long intervalsDone = (secondsSinceStart / intervalSeconds) + 1;
            arrivalTime = startTime.plus(route.getInterval().multipliedBy(intervalsDone));
        }

        return Travel.valueOf(route, timeInRoute, arrivalTime);
    }

    private List<Travel> sortTravels(List<Travel> travels, TravelSortOption sort) {
        return sort == TravelSortOption.TIME_IN_ROUTE ?
            travels.stream().sorted(Comparator.comparing(Travel::getTimeInRoute)).toList() :
            travels.stream().sorted(Comparator.comparing(Travel::getNextArrival)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Route getRouteByName(String name) {
        RouteEntity routeEntity = routeRepository.findByName(name)
            .orElseThrow(() -> new NotFoundException(name, ItemType.ROUTE));

        return mapper.map(routeEntity);
    }

    @Override
    @Transactional
    public Route copyRoute(String routeName, boolean isReverseOrder) {
        Route routeToCopy = getRouteByName(routeName);
        Route copiedRoute = isReverseOrder ? routeToCopy.reverseRoute() : routeToCopy;

        Route copiedRouteWithUpdatedName =
            Route.valueOf(String.format("%s_copy", copiedRoute.getName()), copiedRoute.getType(),
                copiedRoute.getStops(), copiedRoute.getInterval(), copiedRoute.getBusinessHours());

        return addRoute(copiedRouteWithUpdatedName);
    }

    @Override
    @Transactional
    public Route updateRouteByName(String oldRouteName, Route newRoute) {
        if (routeRepository.existsByName(newRoute.getName())) {
            throw new IllegalArgumentException("Route which you want to update already exists");
        }
        if (!routeRepository.existsByName(oldRouteName)) {
            throw new NotFoundException(oldRouteName, ItemType.ROUTE);
        }

        List<String> stopNames =
            StreamSupport.stream(newRoute.getStops().spliterator(), false)
                .map(routeStop -> routeStop.getStop().getName()).toList();
        List<StopEntity> fetchedStops = stopRepository.findAllByNameIn(stopNames);
        if (fetchedStops.size() != stopNames.size()) {
            throw new NotFoundException("Some stop/stops not exists", ItemType.STOP);
        }

        RouteEntity routeEntity = mapper.map(newRoute);
        RouteEntity savedRouteEntity = routeRepository.save(routeEntity);

        return mapper.map(savedRouteEntity);
    }

    @Override
    @Transactional
    public void removeRoute(String routeName) {
        RouteEntity routeEntity = routeRepository.findByName(routeName)
            .orElseThrow(() -> new NotFoundException(routeName, ItemType.ROUTE));

        List<String> stopNames =
            StreamSupport.stream(routeEntity.getStops().spliterator(), false)
                .map(routeStop -> routeStop.getStop().getName()).toList();
        List<StopEntity> fetchedStops = stopRepository.findAllByNameIn(stopNames);
        if (fetchedStops.size() != stopNames.size()) {
            throw new NotFoundException("Some stop/stops not exists", ItemType.STOP);
        }

        routeRepository.deleteByName(routeName);
    }
}
