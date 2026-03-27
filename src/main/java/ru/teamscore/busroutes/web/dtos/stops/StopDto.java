package ru.teamscore.busroutes.web.dtos.stops;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record StopDto(@NotNull UUID id,
                      @NotNull @NotBlank(message = "name must be not blank") String name,
                      @NotNull @Valid GeographicCoordinatesDto coordinates) {
}
