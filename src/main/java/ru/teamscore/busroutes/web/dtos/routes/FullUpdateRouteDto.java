package ru.teamscore.busroutes.web.dtos.routes;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record FullUpdateRouteDto(
    @NotNull
    @NotBlank(message = "name must be not blank")
    String name,
    @NotNull
    @NotBlank(message = "type must be not blank")
    String type,
    @NotNull
    Long interval,
    @NotNull
    @Valid
    BusinessHoursDto businessHours,
    @NotNull
    List<@Valid SummaryRouteStopDto> stops
) {
}
