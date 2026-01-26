package ru.teamscore.busroutes.web.dtos.stops;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record StopDto(
    @NotNull
    @NotBlank(message = "name must be not blank")
    @Length(min = 2, max = 255, message = "Length must be in range 2-255 chars (without blank)")
    String name,
    @NotNull
    @Valid
    GeographicCoordinatesDto coordinates
) {
}
