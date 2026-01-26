package ru.teamscore.busroutes.model.commands;

public record CreateStopCommand(String name, CreateGeographicCoordinatesCommand coordinates) {

    public record CreateGeographicCoordinatesCommand(double latitude, double longitude) {
    }

}
