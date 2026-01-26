package ru.teamscore.busroutes.model.services;

import ru.teamscore.busroutes.model.models.Stop;

public interface StopService {
    Stop addStop(Stop stop);

    Stop getStopByName(String name);

    Stop updateStopByName(String name, Stop stop);

    void removeStopByName(String name);
}
