package ru.teamscore.busroutes.model.mapper;

import org.mapstruct.*;
import ru.teamscore.busroutes.data.entities.StopEntity;
import ru.teamscore.busroutes.model.commands.CreateStopCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateStopCommand;
import ru.teamscore.busroutes.model.models.Stop;

import static org.mapstruct.ReportingPolicy.WARN;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy =
    WARN, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT,
    uses = GeographicCoordinatesMapper.class)
public interface StopMapper {


    @Mapping(source = "coordinates", target = "geographicCoordinates")
    StopEntity map(Stop stop);

    @Mapping(source = "geographicCoordinates", target = "coordinates")
    Stop map(StopEntity stopEntity);

    @Mapping(target = "geographicCoordinates", source = "coordinates")
    StopEntity map(FullUpdateStopCommand stopCommand, @MappingTarget StopEntity entity);

    Stop map(CreateStopCommand createStopCommand);

    Stop map(FullUpdateStopCommand fullUpdateStopCommand);

    @ObjectFactory
    default Stop createStop(StopEntity entity) {
        return Stop.valueOf(entity.getName(), null);
    }

}
