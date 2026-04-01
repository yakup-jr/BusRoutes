package ru.teamscore.busroutes.web.dtos.routes;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.util.UUID;

@Builder
public record BusinessHoursDto(
    @NotNull
    UUID id,
    @NotNull
    @DateTimeFormat(pattern = "HH:mm")
    LocalTime startAt,
    @NotNull
    @DateTimeFormat(pattern = "HH:mm")
    LocalTime endAt
) {
}
