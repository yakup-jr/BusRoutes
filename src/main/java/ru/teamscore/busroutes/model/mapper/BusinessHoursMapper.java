package ru.teamscore.busroutes.model.mapper;

import org.mapstruct.*;
import ru.teamscore.busroutes.data.entities.BusinessHoursEntity;
import ru.teamscore.busroutes.model.models.BusinessHours;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy =
    ReportingPolicy.WARN,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface BusinessHoursMapper {

    BusinessHoursEntity toEntity(BusinessHours businessHours);

    BusinessHours toModel(BusinessHoursEntity entity);

    @ObjectFactory
    default BusinessHours createBusiness(BusinessHoursEntity entity) {
        return BusinessHours.builder().startAt(entity.getStartAt()).endAt(entity.getEndAt())
            .build();
    }

}
