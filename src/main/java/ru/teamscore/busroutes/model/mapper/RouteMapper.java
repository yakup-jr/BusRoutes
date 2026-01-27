package ru.teamscore.busroutes.model.mapper;

import org.mapstruct.*;
import ru.teamscore.busroutes.data.entities.RouteEntity;
import ru.teamscore.busroutes.model.commands.CreateRouteCommand;
import ru.teamscore.busroutes.model.models.Route;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy =
    ReportingPolicy.WARN,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT, uses =
    {BusinessHoursMapper.class, RouteStopMapper.class})
public interface RouteMapper {

    @Mapping(source = "stops", target = "stops")
    @Mapping(source = "businessHours", target = "businessHours")
    RouteEntity toEntity(Route route);

    @Mapping(target = "stops", source = "stops")
    RouteEntity toEntity(CreateRouteCommand command);

    List<RouteEntity> toEntities(List<Route> routes);

    @Mapping(source = "stops", target = "stops")
    @Mapping(source = "businessHours", target = "businessHours")
    Route toModel(RouteEntity routeEntity);

    List<Route> toModels(Iterable<RouteEntity> routeEntity);

    @ObjectFactory
    default Route createRoute(RouteEntity entity) {
        return Route.valueOf(entity.getName(), entity.getType(), null, entity.getInterval(), null);
    }

}
