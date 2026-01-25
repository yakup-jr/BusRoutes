package ru.teamscore.busroutes.model.commands;

public record FullUpdateStopCommand(
    String name,
    FullUpdateGeographicCommand coordinates
) {

    public record FullUpdateGeographicCommand(
        double latitude,
        double longitude
    ) {
    }

}
