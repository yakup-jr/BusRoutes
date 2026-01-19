package ru.teamscore.busroutes.model.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
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
    private final CopyOnWriteArrayList<Route> routes;
    private final StopService stopService;

    @Override
    public Route addRoute(Route route) {
        if (routes.contains(route)) {
            throw new IllegalArgumentException("Route already exists");
        }
        isStopsExists(
            StreamSupport.stream(route.getStops().spliterator(), false).map(RouteStop::getStopName)
                .toList());

        routes.add(route);
        return route;
    }

    private void isStopExists(String stopName) {
        if (!stopService.containsStop(stopName)) {
            throw new NotFoundException(String.format("Stop with name %s not found", stopName),
                ItemType.STOP);
        }
    }

    private void isStopsExists(Iterable<String> stopsName) {
        for (String stopName : stopsName) {
            if (!stopService.containsStop(stopName)) {
                throw new NotFoundException(String.format("Stop with name %s not found", stopName),
                    ItemType.STOP);
            }
        }
    }

    @Override
    public List<Travel> getRoutesByStop(String stopName, TravelSortOption sort) {
        isStopExists(stopName);

        List<Travel> travels = routes.stream().filter(route -> route.containsStop(stopName))
            .map(route -> createTravel(route, stopName)).toList();

        return sortTravels(travels, sort);
    }

    @Override
    public List<Travel> getRoutesByStops(String fromStopName, String toStopName,
                                         TravelSortOption sort) {
        isStopsExists(List.of(fromStopName, toStopName));

        List<Travel> travels = routes.stream()
            .filter(route -> route.containsStop(fromStopName) && route.containsStop(toStopName))
            .map(route -> createTravel(route, fromStopName)).toList();

        return sortTravels(travels, sort);
    }

    @Override
    public boolean isStopInUse(String stopName) {
        isStopExists(stopName);
        return routes.stream().anyMatch(route -> route.containsStop(stopName));
    }

    private Travel createTravel(Route route, String stopName) {
        RouteStop routeStop = StreamSupport.stream(route.getStops().spliterator(), false)
            .filter(rs -> rs.getStopName().equals(stopName)).findFirst()
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
    public Route getRouteByName(String name) {
        Route routeByName =
            routes.stream().filter(route -> route.getName().equals(name)).findFirst()
                .orElseThrow(() -> new NotFoundException(name, ItemType.ROUTE));

        isStopsExists(StreamSupport.stream(routeByName.getStops().spliterator(), false)
            .map(RouteStop::getStopName).toList());

        return routeByName;
    }

    @Override
    public Route copyRoute(String routeName, boolean isReverseOrder) {
        Route routeToCopy = getRouteByName(routeName);
        Route copiedRoute = isReverseOrder ? routeToCopy.reverseRoute() : routeToCopy;

        Route copiedRouteWithUpdatedName =
            Route.valueOf(String.format("%s_copy", copiedRoute.getName()), copiedRoute.getType(),
                copiedRoute.getStops(), copiedRoute.getInterval(), copiedRoute.getBusinessHours());

        return addRoute(copiedRouteWithUpdatedName);
    }

    @Override
    public Route updateRouteByName(String oldRouteName, Route newRoute) {
        if (routes.contains(newRoute)) {
            throw new IllegalArgumentException("Route which you want to update already exists");
        }
        int index = routes.indexOf(getRouteByName(oldRouteName));
        isStopsExists(StreamSupport.stream(newRoute.getStops().spliterator(), false)
            .map(RouteStop::getStopName).toList());

        routes.set(index, newRoute);
        return routes.get(index);
    }

    @Override
    public void removeRoute(String routeName) {
        Route route = getRouteByName(routeName);
        routes.remove(route);
    }
}
