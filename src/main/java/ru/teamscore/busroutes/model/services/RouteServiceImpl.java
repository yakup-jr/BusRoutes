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
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.Travel;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
    public Route addRoute(CreateRouteCommand command) {
        if (routeRepository.existsByName(command.name())) {
            throw new AlreadyExistsException(
                String.format("Route with name %s already exists", command.name()));
        }

        RouteEntity routeEntity = mapper.toEntity(command);

        List<String> stopNames = extractStopNames(command.stops());
        Map<String, StopEntity> stopEntities = getStopEntities(stopNames);
        buildRouteStopRelations(routeEntity, stopEntities);

        return mapper.toModel(routeRepository.save(routeEntity));
    }

    private List<String> extractStopNames(Iterable<RouteStopCommand> stops) {
        return StreamSupport.stream(stops.spliterator(), false).map(RouteStopCommand::stopName)
            .toList();
    }

    private Map<String, StopEntity> getStopEntities(List<String> stopNames) {
        Map<String, StopEntity> stopMap = stopRepository.findAllByNameIn(stopNames).stream()
            .collect(Collectors.toMap(StopEntity::getName, s -> s));

        if (stopMap.size() < stopNames.size()) {
            throw new NotFoundException("Some stops not found", ItemType.STOP);
        }

        return stopMap;
    }

    private void buildRouteStopRelations(RouteEntity routeEntity,
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
        if (!stopRepository.existsByName(stopName)) {
            throw new NotFoundException(stopName, ItemType.STOP);
        }
        List<Route> routes = mapper.toModels(routeRepository.findRoutesByStop(stopName));

        List<Travel> travels =
            routes.stream().map(route -> route.createTravelToLastStop(stopName, clock)).toList();

        return sortTravels(travels, sort);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Travel> getRoutesByStops(String fromStopName, String toStopName,
                                         TravelSortOption sort) {
        if (!stopRepository.existsByName(fromStopName)) {
            throw new NotFoundException(fromStopName, ItemType.STOP);
        }
        if (!stopRepository.existsByName(toStopName)) {
            throw new NotFoundException(toStopName, ItemType.STOP);
        }

        List<Route> routes =
            mapper.toModels(routeRepository.findRoutesByBothStops(fromStopName, toStopName));
        List<Travel> travels = routes.stream()
            .map(route -> route.createTravelBetweenStops(fromStopName, toStopName, clock))
            .filter(Optional::isPresent).map(Optional::get).toList();

        if (travels.isEmpty()) {
            throw new NotFoundException(String.format("from %s to %s", fromStopName, toStopName),
                ItemType.TRAVEL);
        }

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

        return mapper.toModel(routeEntity);
    }

    @Override
    @Transactional
    public Route copyRoute(String routeName, boolean isReverseOrder) {
        RouteEntity routeEntity = routeRepository.findByNameWithStops(routeName)
            .orElseThrow(() -> new NotFoundException(routeName, ItemType.ROUTE));

        Route routeModel = mapper.toModel(routeEntity);
        Route copiedModel = routeModel.copy();
        if (isReverseOrder) {
            copiedModel = copiedModel.reverseRoute();
        }

        RouteEntity newRouteEntity = mapper.toEntity(copiedModel);
        Map<String, StopEntity> stopEntities = extractStopEntities(routeEntity);
        buildRouteStopRelations(newRouteEntity, stopEntities);
        if (newRouteEntity.getBusinessHours() != null) {
            newRouteEntity.getBusinessHours().setId(null);
        }

        return mapper.toModel(routeRepository.save(newRouteEntity));
    }

    @Override
    @Transactional
    public Route updateRouteByName(String name, FullUpdateRouteCommand command) {
        RouteEntity routeEntity = routeRepository.findByName(name)
            .orElseThrow(() -> new NotFoundException(name, ItemType.ROUTE));

        if (!name.equals(command.name()) && routeRepository.existsByName(command.name())) {
            throw new AlreadyExistsException(
                String.format("Route with name %s already exists", command.name()));
        }

        routeEntity.setName(command.name());
        routeEntity.setType(command.type());
        routeEntity.setInterval(command.interval());
        routeEntity.getBusinessHours().setStartAt(command.businessHours().startAt());
        routeEntity.getBusinessHours().setEndAt(command.businessHours().endAt());

        List<String> stopNames = extractStopNames(command.stops());
        Map<String, StopEntity> managedStops = getStopEntities(stopNames);

        updateRouteStops(routeEntity, command.stops(), managedStops);

        return mapper.toModel(routeEntity);
    }

    @Override
    @Transactional
    public void deleteRoute(String routeName) {
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
