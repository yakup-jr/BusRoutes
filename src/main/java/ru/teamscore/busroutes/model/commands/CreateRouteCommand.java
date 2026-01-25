package ru.teamscore.busroutes.model.commands;

import java.time.Duration;

public record CreateRouteCommand(
    String name,
    String type,
    Duration interval,
    BusinessHoursCommand businessHours,
    Iterable<RouteStopCommand> stops) {
}
