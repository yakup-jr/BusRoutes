package ru.teamscore.busroutes.model.services;

import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.Travel;

import java.util.List;

public interface RouteService {
    Route addRoute(Route route);

    List<Travel> getRoutesByStop(String stopName, TravelSortOption sort);

    List<Travel> getRoutesByStops(String fromStopName, String toStopName, TravelSortOption sort);

    Route getRouteByName(String name);

    Route copyRoute(Route route, boolean isReverseOrder);

    Route updateRouteByName(String oldRouteName, Route newRoute);

    void removeRoute(String routeName);
}
