package ru.teamscore.busroutes.web.dtos.routes;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.time.LocalTime;

public record TravelDto(
    @NotNull
    @Valid
    SummaryRouteDto route,
    @NotNull
    Duration timeInRoute,
    @NotNull
    LocalTime nextArrival
    ) {
}
