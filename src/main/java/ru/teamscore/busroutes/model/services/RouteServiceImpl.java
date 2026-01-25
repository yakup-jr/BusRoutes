package ru.teamscore.busroutes.model.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.teamscore.busroutes.data.entities.RouteEntity;
import ru.teamscore.busroutes.data.repositories.RouteRepository;
import ru.teamscore.busroutes.data.repositories.StopRepository;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.exceptions.AlreadyExistsException;
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
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class RouteServiceImpl implements RouteService {
    private final Clock clock;
    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final RouteMapper mapper;

    @Override
    @Transactional
    public Route addRoute(Route route) {
        if (routeRepository.existsByName(route.getName())) {
            throw new IllegalArgumentException("Route already exists");
        }

        validateStopsExists(route.getStops());

        RouteEntity mappedEntity = mapper.map(route);
        RouteEntity savedEntity = routeRepository.save(mappedEntity);
        return mapper.map(savedEntity);
    }

    private void validateStopsExists(Iterable<RouteStop> routeStops) {
        List<String> stopNames = StreamSupport.stream(routeStops.spliterator(), false)
            .map(routeStop -> routeStop.getStop().getName()).toList();

        long stopsCount = stopRepository.countByNameIn(stopNames);
        if (stopsCount != stopNames.size()) {
            throw new NotFoundException("some stops not exists", ItemType.STOP);
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
        Route copiedRoute = routeToCopy.copy();
        copiedRoute = isReverseOrder ? copiedRoute.reverseRoute() : copiedRoute;

        return addRoute(copiedRoute);
    }

    @Override
    @Transactional
    public Route updateRouteByName(String oldRouteName, Route newRoute) {
        if (!oldRouteName.equals(newRoute.getName()) &&
            routeRepository.existsByName(newRoute.getName())) {
            throw new AlreadyExistsException("Route name already exists");
        }

        RouteEntity oldRoute = routeRepository.findByName(oldRouteName)
            .orElseThrow(() -> new NotFoundException(oldRouteName, ItemType.ROUTE));

        validateStopsExists(newRoute.getStops());

        RouteEntity routeEntity = mapper.map(newRoute);
        routeEntity.setId(oldRoute.getId());

        RouteEntity savedRouteEntity = routeRepository.save(routeEntity);
        return mapper.map(savedRouteEntity);
    }

    @Override
    @Transactional
    public void removeRoute(String routeName) {
        if (!routeRepository.existsByName(routeName)) {
            throw new NotFoundException(routeName, ItemType.ROUTE);
        }

        routeRepository.deleteByName(routeName);
    }
}
