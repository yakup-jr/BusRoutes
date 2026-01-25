package ru.teamscore.busroutes.model.commands;

import java.time.Duration;

public record FullUpdateRouteCommand(
    String name,
    String type,
    Duration interval,
    BusinessHoursCommand businessHours,
    Iterable<RouteStopCommand> stops) {

}
