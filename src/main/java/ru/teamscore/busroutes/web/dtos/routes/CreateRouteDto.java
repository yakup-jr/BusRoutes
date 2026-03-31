package ru.teamscore.busroutes.web.dtos.routes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateRouteDto(
    @NotNull(message = "Name must be not null")
    @NotBlank(message = "Name must be not blank")
    String name,
    @NotNull(message = "Type must be not null")
    @NotBlank(message = "Type must be not blank")
    String type,
    @NotNull(message = "Interval must be not null")
    Long interval,
    @Valid
    CreateBusinessHoursDto businessHours,
    @NotNull(message = "Stops must be not null")
    List<@Valid CreateRouteStopDto> stops
) {
}
