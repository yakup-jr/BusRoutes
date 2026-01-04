package ru.teamscore.busroutes.model.services;

import lombok.AllArgsConstructor;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.RouteStop;
import ru.teamscore.busroutes.model.models.Stop;
import ru.teamscore.busroutes.model.models.Travel;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
public class RouteServiceImpl implements RouteService {
    private final List<Route> routes;

    @Override
    public Route addRoute(Route route) {
        routes.add(route);
        return route;
    }

    @Override
    public List<Travel> getRoutesByStop(Stop stop, TravelSortOption sort) {
        if (sort == TravelSortOption.TIME_IN_ROUTE) {

            return routes.stream()
                .filter(route ->
                    route.getStops()
                        .stream().anyMatch(routeStop -> routeStop.getStop().equals(stop))
                )
                .sorted(Comparator.comparing(route ->
                    route.getInterval().minus(Duration.ofSeconds(
                        route.getStops().stream()
                            .filter(routeStop -> routeStop.getStop().equals(stop)).findFirst()
                            .orElseThrow(() -> new NotFoundException(stop.getName(), ItemType.STOP))
                            .getArriveAtFromStart())))
                ).map(route -> {
                    Duration timeInRoute = route.getInterval().minus(Duration.ofSeconds(
                        route.getStops().stream()
                            .filter(routeStop -> routeStop.getStop().equals(stop)).findFirst()
                            .orElseThrow(() -> new NotFoundException(stop.getName(), ItemType.STOP))
                            .getArriveAtFromStart()));
                    LocalTime arrivalTime =
                        route.getBusinessHours().getStartAt();
                    while (LocalTime.now().isAfter(arrivalTime)) {
                        arrivalTime = arrivalTime.plus(route.getInterval());
                    }
                    return Travel.valueOf(route, timeInRoute, arrivalTime);
                }).toList();
        } else {
            return routes.stream()
                .filter(route ->
                    route.getStops()
                        .stream().anyMatch(routeStop -> routeStop.getStop().equals(stop))
                )
                .map(route -> {
                    Duration timeInRoute = route.getInterval().minus(Duration.ofSeconds(
                        route.getStops().stream()
                            .filter(routeStop -> routeStop.getStop().equals(stop)).findFirst()
                            .orElseThrow(() -> new NotFoundException(stop.getName(), ItemType.STOP))
                            .getArriveAtFromStart()));
                    LocalTime arrivalTime =
                        route.getBusinessHours().getStartAt();
                    while (LocalTime.now().isAfter(arrivalTime)) {
                        arrivalTime = arrivalTime.plus(route.getInterval());
                    }
                    return Travel.valueOf(route, timeInRoute, arrivalTime);
                }).sorted(Comparator.comparing(Travel::getNextArrival)).toList();
        }
    }

    @Override
    public List<Travel> getRoutesByStops(Stop from, Stop to, TravelSortOption sort) {
        if (sort == TravelSortOption.TIME_IN_ROUTE) {
            return routes.stream()
                .filter(route ->
                    route.getStops()
                        .stream().anyMatch(routeStop -> routeStop.getStop().equals(from))
                        && route.getStops()
                        .stream().anyMatch(routeStop -> routeStop.getStop().equals(to))
                )
                .sorted(Comparator.comparing(route ->
                    route.getInterval().minus(Duration.ofSeconds(
                        route.getStops().stream()
                            .filter(routeStop -> routeStop.getStop().equals(from)).findFirst()
                            .orElseThrow(() -> new NotFoundException(from.getName(), ItemType.STOP))
                            .getArriveAtFromStart())))
                ).map(route -> {
                    Duration timeInRoute = route.getInterval().minus(Duration.ofSeconds(
                        route.getStops().stream()
                            .filter(routeStop -> routeStop.getStop().equals(from)).findFirst()
                            .orElseThrow(() -> new NotFoundException(from.getName(), ItemType.STOP))
                            .getArriveAtFromStart()));
                    LocalTime arrivalTime =
                        route.getBusinessHours().getStartAt();
                    while (LocalTime.now().isAfter(arrivalTime)) {
                        arrivalTime = arrivalTime.plus(route.getInterval());
                    }
                    return Travel.valueOf(route, timeInRoute, arrivalTime);
                }).toList();
        } else {
            return routes.stream()
                .filter(route ->
                    route.getStops()
                        .stream().anyMatch(routeStop -> routeStop.getStop().equals(from))
                        && route.getStops()
                        .stream().anyMatch(routeStop -> routeStop.getStop().equals(to))
                )
                .map(route -> {
                    Duration timeInRoute = route.getInterval().minus(Duration.ofSeconds(
                        route.getStops().stream()
                            .filter(routeStop -> routeStop.getStop().equals(from)).findFirst()
                            .orElseThrow(() -> new NotFoundException(from.getName(), ItemType.STOP))
                            .getArriveAtFromStart()));
                    LocalTime arrivalTime =
                        route.getBusinessHours().getStartAt();
                    while (LocalTime.now().isAfter(arrivalTime)) {
                        arrivalTime = arrivalTime.plus(route.getInterval());
                    }
                    return Travel.valueOf(route, timeInRoute, arrivalTime);
                }).sorted(Comparator.comparing(Travel::getNextArrival)).toList();
        }
    }

    @Override
    public Route getRouteByName(String name) {
        for (Route route : routes) {
            if (route.getName().equals(name)) {
                return route;
            }
        }
        throw new NotFoundException(name, ItemType.ROUTE);
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
    public Route updateRouteByName(Route oldRoute, Route newRoute) {
        if (!routes.contains(oldRoute)) {
            throw new NotFoundException(oldRoute.getName(), ItemType.ROUTE);
        }
        int index = routes.indexOf(oldRoute);
        routes.set(index, newRoute);
        return routes.get(index);
    }

    @Override
    public void removeRoute(Route removeRoute) {
        routes.remove(removeRoute);
    }
}
