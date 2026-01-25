package ru.teamscore.busroutes.model.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.teamscore.busroutes.data.entities.GeographicCoordinatesEntity;
import ru.teamscore.busroutes.data.entities.StopEntity;
import ru.teamscore.busroutes.data.repositories.RouteRepository;
import ru.teamscore.busroutes.data.repositories.StopRepository;
import ru.teamscore.busroutes.model.commands.CreateStopCommand;
import ru.teamscore.busroutes.model.commands.FullUpdateStopCommand;
import ru.teamscore.busroutes.model.exceptions.AlreadyExistsException;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.mapper.GeographicCoordinatesMapper;
import ru.teamscore.busroutes.model.mapper.StopMapper;
import ru.teamscore.busroutes.model.models.Stop;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StopServiceImplTest {
    @Mock
    private StopRepository stopRepository;
    @Mock
    private RouteRepository routeRepository;
    private StopServiceImpl stopService;
    private final CreateStopCommand createStopCommand = new CreateStopCommand("Ulyanovskaya " +
        "Street", new CreateStopCommand.CreateGeographicCoordinatesCommand(53.198050, 50.108750));
    private final Stop stopModel = Stop.valueOf("Ulyanovskaya Street",
        Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));
    private final StopEntity stopEntity =
        StopEntity.builder()
            .id(UUID.randomUUID())
            .name(stopModel.getName())
            .geographicCoordinates(
                GeographicCoordinatesEntity.builder().id(UUID.randomUUID())
                    .latitude(stopModel.getCoordinates().getLatitude())
                    .longitude(stopModel.getCoordinates().getLongitude()).build())
            .build();


    @BeforeEach
    void setUp() {
        GeographicCoordinatesMapper coordsMapper =
            Mappers.getMapper(GeographicCoordinatesMapper.class);
        StopMapper mapper = Mappers.getMapper(StopMapper.class);

        ReflectionTestUtils.setField(mapper, "geographicCoordinatesMapper", coordsMapper);

        stopService = new StopServiceImpl(stopRepository, routeRepository, mapper);
    }

    @Test
    void addStop() {
        when(stopRepository.existsByName(createStopCommand.name())).thenReturn(false);
        when(stopRepository.save(any(StopEntity.class))).thenAnswer(i -> i.getArgument(0));

        Stop newStop = stopService.addStop(createStopCommand);

        assertThat(newStop).isEqualTo(stopModel);

        verify(stopRepository).existsByName(createStopCommand.name());
        verify(stopRepository).save(any());
    }

    @Test
    void addStop_StopAlreadyExists_ThrowException() {
        when(stopRepository.existsByName(createStopCommand.name())).thenReturn(true);
        assertThatExceptionOfType(AlreadyExistsException.class).isThrownBy(
            () -> stopService.addStop(createStopCommand));

        verify(stopRepository).existsByName(stopModel.getName());
    }

    @Test
    void getStopByName() {
        when(stopRepository.findByName(stopModel.getName())).thenReturn(
            Optional.ofNullable(stopEntity));

        Stop foundStop = stopService.getStopByName(stopModel.getName());

        assertThat(foundStop).isEqualTo(stopModel);
        verify(stopRepository).findByName(stopModel.getName());
    }

    @Test
    void updateStopByName() {
        FullUpdateStopCommand fullUpdateStopCommand = new FullUpdateStopCommand("Ulyanovskaya " +
            "Street 2", new FullUpdateStopCommand.FullUpdateGeographicCommand(54.198050,
            51.108750));
        Stop stopToUpdate = Stop.valueOf("Ulyanovskaya Street 2",
            Stop.GeographicCoordinates.valueOf(54.198050, 51.108750));
        when(stopRepository.existsByName(stopModel.getName())).thenReturn(true);
        when(stopRepository.save(any(StopEntity.class))).thenAnswer(i -> i.getArgument(0));

        Stop updatedStop = stopService.updateStopByName(stopModel.getName(), fullUpdateStopCommand);

        assertThat(updatedStop).isEqualTo(stopToUpdate);
        verify(stopRepository).existsByName(stopModel.getName());
        verify(stopRepository).save(any(StopEntity.class));
    }

    @Test
    void removeStopByName() {
        when(stopRepository.existsByName(stopModel.getName())).thenReturn(true);
        when(routeRepository.existsByStop(stopModel.getName())).thenReturn(false);

        stopService.removeStopByName(stopModel.getName());

        String name = stopModel.getName();
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> stopService.getStopByName(name));
        verify(stopRepository).deleteByName(stopModel.getName());
    }

    @Test
    void removeStopByName_StopNotExists_ThrowException() {
        String stopName = stopModel.getName();
        when(stopRepository.existsByName(stopName)).thenReturn(false);

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> stopService.removeStopByName(stopName));
    }

    @Test
    void removeStopByName_RouteContainStop_ThrowException() {
        String stopName = stopModel.getName();
        when(stopRepository.existsByName(stopName)).thenReturn(true);
        when(routeRepository.existsByStop(stopName)).thenReturn(true);

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
            () -> stopService.removeStopByName(stopName));
    }

}
