package ru.teamscore.busroutes.model.services;

import ru.teamscore.busroutes.model.commands.CreateRouteCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateRouteCommand;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.Travel;

import java.util.List;

public interface RouteService {
    Route addRoute(CreateRouteCommand route);

    List<Travel> getRoutesByStop(String stopName, TravelSortOption sort);

    List<Travel> getRoutesByStops(String fromStopName, String toStopName, TravelSortOption sort);

    Route getRouteByName(String name);

    Route copyRoute(String routeName, boolean isReverseOrder);

    Route updateRouteByName(String oldRouteName, FullUpdateRouteCommand newRoute);

    void removeRoute(String routeName);
}
