package ru.teamscore.busroutes.model.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.teamscore.busroutes.data.entities.RouteEntity;
import ru.teamscore.busroutes.data.entities.RouteStopEntity;
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
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.Travel;

import java.time.Clock;
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

        RouteEntity routeEntity = mapper.map(routeToSave);

        List<String> stopNames = extractStopNames(routeToSave.stops());
        Map<String, StopEntity> managedStops = fetchStops(stopNames);
        buildRouteStopRelation(routeEntity, managedStops);

        return mapper.map(routeRepository.save(routeEntity));
    }

    private List<String> extractStopNames(Iterable<RouteStopCommand> stops) {
        return StreamSupport.stream(stops.spliterator(), false).map(RouteStopCommand::stopName)
            .toList();
    }

    private Map<String, StopEntity> fetchStops(List<String> stopNames) {
        Map<String, StopEntity> stopMap = stopRepository.findAllByNameIn(stopNames).stream()
            .collect(Collectors.toMap(StopEntity::getName, s -> s));

        if (stopMap.size() < stopNames.size()) {
            throw new NotFoundException("Some stops not found", ItemType.STOP);
        }

        return stopMap;
    }

    private void buildRouteStopRelation(RouteEntity routeEntity,
                                        Map<String, StopEntity> managedStops) {
        for (RouteStopEntity routeStop : routeEntity.getStops()) {
            StopEntity managedStop = managedStops.get(routeStop.getStop().getName());
            if (managedStop == null) {
                throw new NotFoundException(routeStop.getStop().getName(), ItemType.STOP);
            }
            routeStop.setStop(managedStop);
            routeStop.setRoute(routeEntity);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Travel> getRoutesByStop(String stopName, TravelSortOption sort) {
        List<Route> routes = mapper.map(routeRepository.findRoutesByStop(stopName));

        List<Travel> travels =
            routes.stream().map(route -> route.createTravelToLastStop(stopName, clock)).toList();

        return sortTravels(travels, sort);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Travel> getRoutesByStops(String fromStopName, String toStopName,
                                         TravelSortOption sort) {
        List<Route> routes =
            mapper.map(routeRepository.findRoutesByBothStops(fromStopName, toStopName));
        List<Travel> travels = routes.stream()
            .map(route -> route.createTravelBetweenStops(fromStopName, toStopName, clock)).toList();

        return sortTravels(travels, sort);
    }

    private List<Travel> sortTravels(List<Travel> travels, TravelSortOption sort) {
        return sort == TravelSortOption.TIME_IN_ROUTE ?
            travels.stream().sorted(Comparator.comparing(Travel::getTimeInRoute)).toList() :
            travels.stream().sorted(Comparator.comparing(Travel::getNextArrival)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Route getRouteByName(String name) {
        RouteEntity routeEntity = routeRepository.findByNameWithStops(name)
            .orElseThrow(() -> new NotFoundException(name, ItemType.ROUTE));

        return mapper.map(routeEntity);
    }

    @Override
    @Transactional
    public Route copyRoute(String routeName, boolean isReverseOrder) {
        RouteEntity routeEntity = routeRepository.findByNameWithStops(routeName)
            .orElseThrow(() -> new NotFoundException(routeName, ItemType.ROUTE));

        Route routeModel = mapper.map(routeEntity);
        Route copiedModel = routeModel.copy();
        if (isReverseOrder) {
            copiedModel = copiedModel.reverseRoute();
        }

        RouteEntity newRouteEntity = mapper.map(copiedModel);
        Map<String, StopEntity> managedStops = extractStopEntities(routeEntity);
        buildRouteStopRelation(newRouteEntity, managedStops);
        if (newRouteEntity.getBusinessHours() != null) {
            newRouteEntity.getBusinessHours().setId(null);
        }

        return mapper.map(routeRepository.save(newRouteEntity));
    }

    @Override
    @Transactional
    public Route updateRouteByName(String oldRouteName, FullUpdateRouteCommand routeToUpdate) {
        if (!oldRouteName.equals(routeToUpdate.name()) &&
            routeRepository.existsByName(routeToUpdate.name())) {
            throw new AlreadyExistsException("Route name already exists");
        }

        RouteEntity routeEntity = routeRepository.findByName(oldRouteName)
            .orElseThrow(() -> new NotFoundException(oldRouteName, ItemType.ROUTE));

        routeEntity.setName(routeToUpdate.name());
        routeEntity.setType(routeToUpdate.type());
        routeEntity.setInterval(routeToUpdate.interval());
        routeEntity.getBusinessHours().setStartAt(routeToUpdate.businessHours().startAt());
        routeEntity.getBusinessHours().setEndAt(routeToUpdate.businessHours().endAt());

        List<String> stopNames = extractStopNames(routeToUpdate.stops());
        Map<String, StopEntity> managedStops = fetchStops(stopNames);

        updateRouteStops(routeEntity, routeToUpdate.stops(), managedStops);

        return mapper.map(routeEntity);
    }

    @Override
    @Transactional
    public void removeRoute(String routeName) {
        if (!routeRepository.existsByName(routeName)) {
            throw new NotFoundException(routeName, ItemType.ROUTE);
        }

        routeRepository.deleteByName(routeName);
    }

    private Map<String, StopEntity> extractStopEntities(RouteEntity routeEntity) {
        return routeEntity.getStops().stream().map(RouteStopEntity::getStop)
            .collect(Collectors.toMap(StopEntity::getName, s -> s, (s1, s2) -> s1));
    }

    private void updateRouteStops(RouteEntity routeEntity, Iterable<RouteStopCommand> routeStops,
                                  Map<String, StopEntity> stopEntities) {
        routeEntity.getStops().clear();

        for (RouteStopCommand routeStopCommand : routeStops) {
            StopEntity stop = stopEntities.get(routeStopCommand.stopName());
            if (stop == null) {
                throw new NotFoundException(routeStopCommand.stopName(), ItemType.STOP);
            }

            RouteStopEntity routeStop = RouteStopEntity.builder().stop(stop).route(routeEntity)
                .stopOrder(routeStopCommand.stopOrder())
                .arriveAtFromStart(routeStopCommand.arriveAtFromStart()).build();

            routeEntity.getStops().add(routeStop);
        }
    }
}
