package ru.teamscore.busroutes.model.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.teamscore.busroutes.data.entities.RouteEntity;
import ru.teamscore.busroutes.data.entities.StopEntity;
import ru.teamscore.busroutes.data.repositories.RouteRepository;
import ru.teamscore.busroutes.data.repositories.StopRepository;
import ru.teamscore.busroutes.model.commands.CreateRouteCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateRouteCommand;
import ru.teamscore.busroutes.model.commands.RouteStopCommand;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.exceptions.AlreadyExistsException;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.mapper.RouteMapper;
import ru.teamscore.busroutes.model.mapper.StopMapper;
import ru.teamscore.busroutes.model.models.BusinessHours;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.RouteStop;
import ru.teamscore.busroutes.model.models.Travel;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class RouteServiceImpl implements RouteService {
    private final Clock clock;
    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final StopMapper stopMapper;
    private final RouteMapper mapper;

    @Override
    @Transactional
    public Route addRoute(CreateRouteCommand routeToSave) {
        if (routeRepository.existsByName(routeToSave.name())) {
            throw new IllegalArgumentException("Route already exists");
        }

        List<String> stopNames =
            StreamSupport.stream(routeToSave.stops().spliterator(), false)
                .map(RouteStopCommand::stopName).toList();
        List<RouteStop> routeStops = getRouteStopsByStop(stopNames, routeToSave.stops());

        Route route = Route.valueOf(routeToSave.name(), routeToSave.type(), routeStops,
            routeToSave.interval(),
            BusinessHours.valueOf(routeToSave.businessHours().startAt(),
                routeToSave.businessHours().endAt()));

        return saveRoute(route);
    }

    private Route saveRoute(Route route) {
        RouteEntity mappedEntity = mapper.map(route);
        RouteEntity savedEntity = routeRepository.save(mappedEntity);
        return mapper.map(savedEntity);
    }

    private List<RouteStop> getRouteStopsByStop(
        List<String> stopNames, Iterable<RouteStopCommand> routeStop) {
        Map<String, StopEntity> stopEntityMap = stopRepository.findAllByNameIn(stopNames).stream()
            .collect(Collectors.toMap(StopEntity::getName, stopEntity -> stopEntity));

        if (stopEntityMap.size() < stopNames.size()) {
            throw new NotFoundException("Some stops not found", ItemType.STOP);
        }

        return StreamSupport.stream(routeStop.spliterator(), false)
            .map(rs -> RouteStop.valueOf(
                rs.arriveAtFromStart(),
                rs.stopOrder(),
                stopMapper.map(stopEntityMap.get(rs.stopName()))
            )).toList();
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
        if (isReverseOrder) {
            copiedRoute = copiedRoute.reverseRoute();
        }

        return saveRoute(copiedRoute);
    }

    @Override
    @Transactional
    public Route updateRouteByName(String oldRouteName, FullUpdateRouteCommand routeToUpdate) {
        if (!oldRouteName.equals(routeToUpdate.name()) &&
            routeRepository.existsByName(routeToUpdate.name())) {
            throw new AlreadyExistsException("Route name already exists");
        }

        RouteEntity oldRoute = routeRepository.findByName(oldRouteName)
            .orElseThrow(() -> new NotFoundException(oldRouteName, ItemType.ROUTE));

        List<String> stopNames = StreamSupport.stream(routeToUpdate.stops().spliterator(), false)
            .map(RouteStopCommand::stopName).toList();
        List<RouteStop> routeStops = getRouteStopsByStop(stopNames, routeToUpdate.stops());

        Route route = Route.valueOf(routeToUpdate.name(), routeToUpdate.type(), routeStops,
            routeToUpdate.interval(),
            BusinessHours.valueOf(routeToUpdate.businessHours().startAt(),
                routeToUpdate.businessHours().endAt()));

        RouteEntity routeEntity = mapper.map(route);
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
