package ru.teamscore.busroutes.web.dtos.stops;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record GeographicCoordinatesDto(
    @DecimalMin(value = "-90", message = "Latitude must be at least -90")
    @DecimalMax(value = "90", message = "Latitude must be no more 90")
    double latitude,
    @DecimalMin(value = "-180", message = "Longitude must be at least -180")
    @DecimalMax(value = "180", message = "Longitude must be no more 180")
    double longitude) {
}
