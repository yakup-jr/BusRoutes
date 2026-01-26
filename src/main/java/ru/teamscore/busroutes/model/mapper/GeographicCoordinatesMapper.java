package ru.teamscore.busroutes.model.mapper;

import org.mapstruct.*;
import ru.teamscore.busroutes.data.entities.GeographicCoordinatesEntity;
import ru.teamscore.busroutes.model.models.Stop;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy =
    ReportingPolicy.WARN,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface GeographicCoordinatesMapper {

    GeographicCoordinatesEntity map(Stop.GeographicCoordinates geographicCoordinates);

    Stop.GeographicCoordinates map(GeographicCoordinatesEntity geographicCoordinatesEntity);

    @ObjectFactory
    default Stop.GeographicCoordinates createGeographicCoordinates(
        GeographicCoordinatesEntity entity) {
        return Stop.GeographicCoordinates.valueOf(entity.getLatitude(), entity.getLongitude());
    }
}
