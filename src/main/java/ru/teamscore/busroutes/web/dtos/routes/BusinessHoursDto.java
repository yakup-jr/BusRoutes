package ru.teamscore.busroutes.web.dtos.routes;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record BusinessHoursDto(
    @NotNull
    LocalTime startAt,
    @NotNull
    LocalTime endAt
) {
}
