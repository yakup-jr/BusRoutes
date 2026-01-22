package ru.teamscore.busroutes.model.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.teamscore.busroutes.data.entities.StopEntity;
import ru.teamscore.busroutes.data.repositories.StopRepository;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.mapper.StopMapper;
import ru.teamscore.busroutes.model.models.Stop;

@Service
@AllArgsConstructor
public class StopServiceImpl implements StopService {
    private final StopRepository stopRepository;
    private final StopMapper mapper;

    @Override
    @Transactional
    public Stop addStop(Stop stop) {
        if (stopRepository.existsByName(stop.getName())) {
            //todo: change to AlreadyExistsException
            throw new IllegalArgumentException("Stop already exists");
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

    //todo: think about needing this method in interface, cause now it possible to inject
    // repository instead of using this method
    @Override
    @Transactional(readOnly = true)
    public boolean containsStop(String stopName) {
        return stopRepository.existsByName(stopName);
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
        stopRepository.deleteByName(name);
    }
}
