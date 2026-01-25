package ru.teamscore.busroutes.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.teamscore.busroutes.model.models.Route;
import ru.teamscore.busroutes.model.models.RouteStop;
import ru.teamscore.busroutes.model.models.Travel;
import ru.teamscore.busroutes.web.dtos.routes.*;

import static org.mapstruct.ReportingPolicy.WARN;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy =
    WARN, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface RouteDtoMapper {

    Route map(CreateRouteDto createRouteDto);

    Route map(FullUpdateRouteDto fullUpdateRouteDto);

    SummaryRouteDto map(Route route);

    @Mapping(source = "stopName", target = "stop.name")
    RouteStop map(SummaryRouteStopDto routeStopDto);

    @Mapping(source = "stop.name", target = "stopName")
    SummaryRouteStopDto map(RouteStop routeStop);

    TravelDto map(Travel travel);

    Iterable<TravelDto> map(Iterable<Travel> travels);

}
