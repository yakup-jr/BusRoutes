package ru.teamscore.busroutes.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.teamscore.busroutes.model.commands.CreateRouteCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateRouteCommand;
import ru.teamscore.busroutes.model.commands.RouteStopCommand;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.RouteStop;
import ru.teamscore.busroutes.model.models.Travel;
import ru.teamscore.busroutes.web.dtos.routes.*;

import static org.mapstruct.ReportingPolicy.WARN;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = WARN,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface RouteDtoMapper {

    CreateRouteCommand toCommand(CreateRouteDto createRouteDto);

    FullUpdateRouteCommand toCommand(FullUpdateRouteDto fullUpdateRouteDto);

    @Mapping(source = "stop.name", target = "stopName")
    SummaryRouteStopDto toDto(RouteStop routeStop);

    Iterable<RouteStopCommand> toCommands(Iterable<SummaryRouteStopDto> routeStopDto);

    RouteStopCommand toCommand(SummaryRouteStopDto routeStopDto);

    @Mapping(source = "stopName", target = "stopName")
    SummaryRouteStopDto toDto(RouteStopCommand routeStop);

    SummaryRouteDto toDto(Route route);

    TravelDto toDto(Travel travel);

    Iterable<TravelDto> toDtos(Iterable<Travel> travels);

}
