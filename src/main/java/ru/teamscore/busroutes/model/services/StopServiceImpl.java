package ru.teamscore.busroutes.model.services;

import org.springframework.stereotype.Service;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.models.Stop;

import java.util.List;

@Service
public class StopServiceImpl implements StopService {
    private final List<Stop> stops;

    public StopServiceImpl(List<Stop> stops) {
        this.stops = stops;
    }

    @Override
    public Stop addStop(Stop stop) {
        stops.add(stop);
        return stop;
    }

    @Override
    public Stop getStopByName(String name) {
        for (Stop stop : stops) {
            if (stop.getName().equals(name)) {
                return stop;
            }
        }
        throw new NotFoundException(name, ItemType.STOP);
    }

    @Override
    public Stop updateStopByName(String name, Stop stop) {
        for (int i = 0; i < stops.size(); i++) {
            if (stops.get(i).getName().equals(name)) {
                stops.set(i, stop);
                return stop;
            }
        }
        throw new NotFoundException(name, ItemType.STOP);
    }

    @Override
    public void removeStopByName(String name) {
        if (getStopByName(name) == null) {
            throw new NotFoundException(name, ItemType.STOP);
        }
        stops.removeIf(stop -> stop.getName().equals(name));
    }
}
