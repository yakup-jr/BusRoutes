package ru.teamscore.busroutes.model.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.RouteStop;
import ru.teamscore.busroutes.model.models.Travel;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class RouteServiceImpl implements RouteService {
    private final List<Route> routes;

    @Override
    public Route addRoute(Route route) {
        routes.add(route);
        return route;
    }

    @Override
    public List<Travel> getRoutesByStop(String stopName, TravelSortOption sort) {
        List<Travel> travels = routes.stream()
            .filter(route -> route.getStops().stream()
                .anyMatch(routeStop -> routeStop.getStop().getName().equals(stopName)))
            .map(route -> createTravel(route, stopName))
            .toList();

        return sort == TravelSortOption.TIME_IN_ROUTE
            ? travels.stream().sorted(Comparator.comparing(Travel::getTimeInRoute)).toList()
            : travels.stream().sorted(Comparator.comparing(Travel::getNextArrival)).toList();
    }

    @Override
    public List<Travel> getRoutesByStops(String fromStopName, String toStopName, TravelSortOption sort) {
        List<Travel> travels = routes.stream()
            .filter(route -> route.getStops().stream()
                .anyMatch(routeStop -> routeStop.getStop().getName().equals(fromStopName))
                && route.getStops().stream()
                .anyMatch(routeStop -> routeStop.getStop().getName().equals(toStopName)))
            .map(route -> createTravel(route, fromStopName))
            .toList();

        return sort == TravelSortOption.TIME_IN_ROUTE
            ? travels.stream().sorted(Comparator.comparing(Travel::getTimeInRoute)).toList()
            : travels.stream().sorted(Comparator.comparing(Travel::getNextArrival)).toList();
    }

    private Travel createTravel(Route route, String stopName) {
        RouteStop routeStop = route.getStops().stream()
            .filter(rs -> rs.getStop().getName().equals(stopName))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(stopName, ItemType.STOP));

        Duration timeInRoute = route.getInterval().minus(Duration.ofSeconds(routeStop.getArriveAtFromStart()));
        LocalTime arrivalTime = route.getBusinessHours().getStartAt();
        while (LocalTime.now().isAfter(arrivalTime)) {
            arrivalTime = arrivalTime.plus(route.getInterval());
        }
        return Travel.valueOf(route, timeInRoute, arrivalTime);
    }

    @Override
    public Route getRouteByName(String name) {
        return routes.stream()
            .filter(route -> route.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(name, ItemType.ROUTE));
    }

    @Override
    public Route copyRoute(Route route, boolean isReverseOrder) {
        if (isReverseOrder) {
            List<RouteStop> reversedStops = new ArrayList<>();
            for (int i = route.getStops().size() - 1; i > -1; i--) {
                reversedStops.add(route.getStops().get(i));
            }
            return Route.valueOf(route.getName(), route.getType(), reversedStops,
                route.getInterval(), route.getBusinessHours());
        }
        return Route.valueOf(route.getName(), route.getType(), List.copyOf(route.getStops()),
            route.getInterval(), route.getBusinessHours());
    }

    @Override
    public Route updateRouteByName(String oldRouteName, Route newRoute) {
        int index = routes.indexOf(getRouteByName(oldRouteName));
        routes.set(index, newRoute);
        return routes.get(index);
    }

    @Override
    public void removeRoute(String routeName) {
        Route route = getRouteByName(routeName);

        routes.remove(route);
    }
}
