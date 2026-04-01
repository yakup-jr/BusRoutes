package ru.teamscore.busroutes.web.dtos.routes;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

@Builder
public record CreateBusinessHoursDto(
    @NotNull(message = "start at must be not null")
    @DateTimeFormat(pattern = "HH:mm")
    LocalTime startAt,
    @NotNull(message = "end at must be not null")
    @DateTimeFormat(pattern = "HH:mm")
    LocalTime endAt) {
}
