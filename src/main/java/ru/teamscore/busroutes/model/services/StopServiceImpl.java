package ru.teamscore.busroutes.model.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.models.Stop;

import java.util.concurrent.CopyOnWriteArrayList;

@Service
@AllArgsConstructor
public class StopServiceImpl implements StopService {
    private final CopyOnWriteArrayList<Stop> stops;

    @Override
    public Stop addStop(Stop stop) {
        if (stops.contains(stop)) {
            throw new IllegalArgumentException("Stop already exists");
        }
        stops.add(stop);
        return stop;
    }

    @Override
    public Stop getStopByName(String name) {
        return stops.stream()
            .filter(stop -> stop.getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(name, ItemType.STOP));

    }

    @Override
    public boolean containsStop(String stopName) {
        return stops.stream().anyMatch(stop -> stop.getName().equals(stopName));
    }

    @Override
    public Stop updateStopByName(String oldName, Stop stop) {
        int index = stops.indexOf(getStopByName(oldName));
        stops.set(index, stop);
        return stops.get(index);
    }

    @Override
    public void removeStopByName(String name) {
        Stop stop = getStopByName(name);
        stops.remove(stop);
    }
}
