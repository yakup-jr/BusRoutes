package ru.teamscore.busroutes.model.mapper;

import org.mapstruct.*;
import ru.teamscore.busroutes.data.entities.RouteStopEntity;
import ru.teamscore.busroutes.model.commands.RouteStopCommand;
import ru.teamscore.busroutes.model.models.RouteStop;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy =
    ReportingPolicy.WARN,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT,
    uses = StopMapper.class)
public interface RouteStopMapper {

    @Mapping(source = "stop", target = "stop")
    RouteStopEntity map(RouteStop routeStop);

    @Mapping(source = "stop", target = "stop")
    RouteStop map(RouteStopEntity routeStopEntity);

    @Mapping(target = "stop.name", source = "stopName")
    @Mapping(target = "route", ignore = true)
    RouteStopEntity map(RouteStopCommand command);

    @ObjectFactory
    default RouteStop createRouteStop(RouteStopEntity entity) {
        return RouteStop.valueOf(entity.getArriveAtFromStart(), entity.getStopOrder(),
            null);
    }

}
