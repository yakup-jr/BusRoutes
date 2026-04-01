package ru.teamscore.busroutes.model.services;

import org.springframework.data.domain.Page;
import ru.teamscore.busroutes.model.commands.CreateRouteCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateRouteCommand;
import ru.teamscore.busroutes.model.enums.TravelSortOption;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.Travel;

import java.util.List;
import java.util.UUID;

public interface RouteService {
    Route addRoute(CreateRouteCommand command);

    Page<Route> getRoutes(int page, int size);

    List<Travel> getRoutesByStop(String stopName, TravelSortOption sort);

    Route getRouteById(UUID id);

    List<Travel> getRoutesByStops(String fromStopName, String toStopName, TravelSortOption sort);

    Iterable<Route> getRouteByName(String name);

    Route copyRoute(UUID routeId, boolean isReverseOrder);

    Route updateRoute(UUID routeId, FullUpdateRouteCommand command);

    void deleteRouteByName(String routeName);

    void deleteRouteById(UUID id);
}
