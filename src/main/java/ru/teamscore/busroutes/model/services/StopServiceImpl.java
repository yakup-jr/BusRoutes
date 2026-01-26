package ru.teamscore.busroutes.model.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.teamscore.busroutes.data.entities.StopEntity;
import ru.teamscore.busroutes.data.repositories.RouteRepository;
import ru.teamscore.busroutes.data.repositories.StopRepository;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.exceptions.AlreadyExistsException;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.mapper.StopMapper;
import ru.teamscore.busroutes.model.models.Stop;

@Service
@AllArgsConstructor
public class StopServiceImpl implements StopService {
    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final StopMapper mapper;

    @Override
    @Transactional
    public Stop addStop(Stop stop) {
        if (stopRepository.existsByName(stop.getName())) {
            throw new AlreadyExistsException("Stop already exists");
        }

        StopEntity mappedStopEntity = mapper.map(stop);
        StopEntity savedStopEntity = stopRepository.save(mappedStopEntity);
        return mapper.map(savedStopEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Stop getStopByName(String name) {
        StopEntity foundStopEntity = stopRepository.findByName(name)
            .orElseThrow(() -> new NotFoundException(name, ItemType.STOP));

        return mapper.map(foundStopEntity);
    }

    @Override
    @Transactional
    public Stop updateStopByName(String oldName, Stop updatedStop) {
        if (!stopRepository.existsByName(oldName)) {
            throw new NotFoundException(oldName, ItemType.STOP);
        }

        StopEntity mappedStopEntity = mapper.map(updatedStop);
        StopEntity savedStopEntity = stopRepository.save(mappedStopEntity);
        return mapper.map(savedStopEntity);
    }

    @Override
    @Transactional
    public void removeStopByName(String name) {
        if (!stopRepository.existsByName(name)) {
            throw new NotFoundException(name, ItemType.STOP);
        }
        if (routeRepository.existsByStop(name)) {
            throw new IllegalArgumentException(String.format(
                "%s exists in route. You can't remove stop until it " +
                    "hasn't relation with any route", name));
        }
        stopRepository.deleteByName(name);
    }
}
