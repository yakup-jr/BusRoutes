package ru.teamscore.busroutes.model.mapper;

import org.mapstruct.*;
import ru.teamscore.busroutes.data.entities.StopEntity;
import ru.teamscore.busroutes.model.commands.CreateStopCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateStopCommand;
import ru.teamscore.busroutes.model.models.Stop;

import static org.mapstruct.ReportingPolicy.WARN;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = WARN,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT,
    uses = GeographicCoordinatesMapper.class)
public interface StopMapper {

    @Mapping(source = "coordinates", target = "geographicCoordinates")
    StopEntity toEntity(Stop stop);

    @Mapping(source = "geographicCoordinates", target = "coordinates")
    Stop toModel(StopEntity stopEntity);

    @Mapping(target = "geographicCoordinates", source = "coordinates")
    StopEntity toEntity(FullUpdateStopCommand stopCommand, @MappingTarget StopEntity entity);

    Stop toModel(CreateStopCommand createStopCommand);

    Stop toModel(FullUpdateStopCommand fullUpdateStopCommand);

    @ObjectFactory
    default Stop createStop(StopEntity entity) {
        return Stop.valueOf(entity.getName(), null);
    }

}
