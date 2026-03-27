package ru.teamscore.busroutes.web.dtos.routes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;

public record CreateRouteDto(
    @NotNull
    @NotBlank(message = "name must be not blank")
    String name,
    @NotNull
    @NotBlank(message = "type must be not blank")
    String type,
    @NotNull
    Duration interval,
    @NotNull
    @Valid
    CreateBusinessHoursDto businessHours,
    @NotNull
    Iterable<@Valid SummaryRouteStopDto> stops
) {
}
