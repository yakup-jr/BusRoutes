package ru.teamscore.busroutes.model.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.teamscore.busroutes.data.entities.StopEntity;
import ru.teamscore.busroutes.data.repositories.RouteRepository;
import ru.teamscore.busroutes.data.repositories.StopRepository;
import ru.teamscore.busroutes.model.commands.CreateStopCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateStopCommand;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.exceptions.AlreadyExistsException;
import ru.teamscore.busroutes.model.exceptions.InUseException;
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
    public Stop addStop(CreateStopCommand command) {
        if (stopRepository.existsByName(command.name())) {
            throw new AlreadyExistsException(
                String.format("Stop with name %s already exists", command.name()));
        }

        Stop stopModel = mapper.toModel(command);
        StopEntity stopEntity = mapper.toEntity(stopModel);
        StopEntity savedStopEntity = stopRepository.save(stopEntity);
        return mapper.toModel(savedStopEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Stop getStopByName(String name) {
        StopEntity foundStopEntity = stopRepository.findByName(name)
            .orElseThrow(() -> new NotFoundException(name, ItemType.STOP));

        return mapper.toModel(foundStopEntity);
    }

    @Override
    @Transactional
    public Stop updateStopByName(String name, FullUpdateStopCommand command) {
        if (!name.equals(command.name()) && stopRepository.existsByName(command.name())) {
            throw new AlreadyExistsException(String.format("Stop with name %s already exists",
                command.name()));
        }
        StopEntity stopEntity = stopRepository.findByName(name)
            .orElseThrow(() -> new NotFoundException(name, ItemType.STOP));

        StopEntity stopToUpdate = mapper.toEntity(command, stopEntity);
        StopEntity savedStopEntity = stopRepository.save(stopToUpdate);
        return mapper.toModel(savedStopEntity);
    }

    @Override
    @Transactional
    public void deleteStopByName(String name) {
        if (!stopRepository.existsByName(name)) {
            throw new NotFoundException(name, ItemType.STOP);
        }
        if (routeRepository.existsByStop(name)) {
            throw new InUseException(String.format(
                "%s exists in route. You can't delete stop until it " +
                    "hasn't relation with any route", name));
        }
        stopRepository.deleteByName(name);
    }
}
