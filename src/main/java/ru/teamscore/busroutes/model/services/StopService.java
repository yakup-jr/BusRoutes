package ru.teamscore.busroutes.model.services;

import ru.teamscore.busroutes.model.commands.CreateStopCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateStopCommand;
import ru.teamscore.busroutes.model.models.Stop;

public interface StopService {
    Stop addStop(CreateStopCommand stop);

    Stop getStopByName(String name);

    Stop updateStopByName(String name, FullUpdateStopCommand stop);

    void removeStopByName(String name);
}
