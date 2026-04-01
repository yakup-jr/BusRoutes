package ru.teamscore.busroutes.model.services;

import org.springframework.data.domain.Page;
import ru.teamscore.busroutes.model.commands.CreateStopCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateStopCommand;
import ru.teamscore.busroutes.model.models.Stop;

import java.util.UUID;

public interface StopService {
    Stop addStop(CreateStopCommand command);

    Stop getStopByName(String name);

    Stop getStopById(UUID id);

    Page<Stop> getStops(int page, int size);

    Page<Stop> getStopsByNameStartingWithIgnoreCase(String name, int page, int size);

    Stop updateStopByName(String name, FullUpdateStopCommand command);

    void deleteStopByName(String name);
}
