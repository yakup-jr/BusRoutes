package ru.teamscore.busroutes.model.commands;

import java.time.LocalTime;

public record BusinessHoursCommand(LocalTime startAt, LocalTime endAt) {
}
