package ru.teamscore.busroutes.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.teamscore.busroutes.model.commands.CreateStopCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateStopCommand;
import ru.teamscore.busroutes.model.models.Stop;
import ru.teamscore.busroutes.web.dtos.stops.CreateStopDto;
import ru.teamscore.busroutes.web.dtos.stops.FullUpdateStopDto;
import ru.teamscore.busroutes.web.dtos.stops.StopDto;

import static org.mapstruct.ReportingPolicy.WARN;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy =
    WARN, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface StopDtoMapper {

    StopDto toDto(Stop stop);

    FullUpdateStopCommand toCommand(FullUpdateStopDto fullUpdateStopDto);

    CreateStopCommand toCommand(CreateStopDto createStopDto);

}
