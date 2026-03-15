package ru.teamscore.busroutes.web.dtos.routes;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalTime;

@Builder
public record CreateBusinessHoursDto(@NotNull LocalTime startAt, @NotNull LocalTime endAt) {
}
