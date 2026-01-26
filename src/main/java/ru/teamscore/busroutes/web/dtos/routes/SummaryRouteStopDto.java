package ru.teamscore.busroutes.web.dtos.routes;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record SummaryRouteStopDto(
    @Min(value = 0, message = "time time from start must be non negative")
    int arriveAtFromStart,
    @Min(value = 1, message = "order must be positive")
    int stopOrder,
    @NotNull
    @NotBlank(message = "name must be not blank")
    @Length(min = 2, max = 255, message = "Length must be in range 2-255 chars (without blank)")
    String stopName
) {
}
