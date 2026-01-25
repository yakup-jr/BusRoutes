package ru.teamscore.busroutes.model.commands;

public record RouteStopCommand(int arriveAtFromStart, int stopOrder, String stopName) {
}
