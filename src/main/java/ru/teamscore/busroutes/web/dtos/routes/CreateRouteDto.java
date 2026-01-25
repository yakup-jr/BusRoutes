package ru.teamscore.busroutes.web.dtos.routes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.Duration;

public record CreateRouteDto(
    @NotNull
    @NotBlank(message = "name must be not blank")
    @Length(min = 2, max = 255, message = "length of route name must be between 2 and 255")
    String name,
    @NotNull
    @NotBlank(message = "type must be not blank")
    @Length(min = 2, max = 255, message = "length of route type must be between 2 and 255")
    String type,
    @NotNull
    Duration interval,
    @NotNull
    @Valid
    BusinessHoursDto businessHours,
    @NotNull
    Iterable<@Valid SummaryRouteStopDto> stops
) {
}
