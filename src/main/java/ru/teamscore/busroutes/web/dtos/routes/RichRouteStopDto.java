package ru.teamscore.busroutes.web.dtos.routes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ru.teamscore.busroutes.web.dtos.stops.StopDto;

import java.util.UUID;

public record RichRouteStopDto(
    @NotNull
    UUID id,
    @Min(value = 0, message = "time time from start must be non negative")
    int arriveAtFromStart,
    @Min(value = 1, message = "order must be positive")
    int stopOrder,
    @NotNull
    @Valid
    StopDto stop
) {
}
