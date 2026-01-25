package ru.teamscore.busroutes.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.teamscore.busroutes.data.entities.GeographicCoordinatesEntity;
import ru.teamscore.busroutes.model.models.Stop;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy =
    ReportingPolicy.WARN,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface GeographicCoordinatesMapper {

    GeographicCoordinatesEntity map(Stop.GeographicCoordinates geographicCoordinates);

    Stop.GeographicCoordinates map(GeographicCoordinatesEntity geographicCoordinatesEntity);
}
