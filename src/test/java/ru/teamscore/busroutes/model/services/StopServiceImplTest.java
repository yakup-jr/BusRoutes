package ru.teamscore.busroutes.model.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import ru.teamscore.busroutes.model.exceptions.InUseException;
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

    private static final String STOP_NAME = "Ulyanovskaya Street";
    private static final double LAT = 53.198050;
    private static final double LON = 50.108750;

    @BeforeEach
    void setUp() {
        GeographicCoordinatesMapper coordsMapper =
            Mappers.getMapper(GeographicCoordinatesMapper.class);
        StopMapper mapper = Mappers.getMapper(StopMapper.class);

        ReflectionTestUtils.setField(mapper, "geographicCoordinatesMapper", coordsMapper);

        stopService = new StopServiceImpl(stopRepository, routeRepository, mapper);
    }

    @Nested
    class StopAddition {
        @Test
        void addStop() {
            var command = createCreateCommand();
            when(stopRepository.existsByName(STOP_NAME)).thenReturn(false);
            when(stopRepository.save(any(StopEntity.class))).thenAnswer(i -> i.getArgument(0));

            Stop result = stopService.addStop(command);

            assertThat(result.getName()).isEqualTo(STOP_NAME);
            assertThat(result.getCoordinates().getLatitude()).isEqualTo(LAT);
            verify(stopRepository).save(any());
        }

        @Test
        void addStop_StopAlreadyExists_ThrowException() {
            var command = createCreateCommand();
            when(stopRepository.existsByName(STOP_NAME)).thenReturn(true);

            assertThatExceptionOfType(AlreadyExistsException.class).isThrownBy(
                () -> stopService.addStop(command));
        }
    }

    @Nested
    class StopRetrieval {

        @Test
        void getStopByName() {
            StopEntity entity = createStopEntity();
            when(stopRepository.findByName(STOP_NAME)).thenReturn(Optional.of(entity));

            Stop result = stopService.getStopByName(STOP_NAME);

            assertThat(result.getName()).isEqualTo(STOP_NAME);
            assertThat(result.getCoordinates().getLatitude()).isEqualTo(LAT);
        }

        @Test
        void getStopByName_StopNotExists_ThrowException() {
            when(stopRepository.findByName(STOP_NAME)).thenReturn(Optional.empty());

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> stopService.getStopByName(STOP_NAME));
        }
    }

    @Nested
    class StopUpdate {

        @Test
        void updateStopByName() {
            String oldName = STOP_NAME;
            String newName = "Ulyanovskaya Street 2";
            double newLat = 54.198050;
            double newLon = 51.108750;

            FullUpdateStopCommand command = new FullUpdateStopCommand(newName,
                new FullUpdateStopCommand.FullUpdateGeographicCommand(newLat, newLon));

            when(stopRepository.findByName(oldName)).thenReturn(Optional.of(createStopEntity()));
            when(stopRepository.save(any(StopEntity.class))).thenAnswer(i -> i.getArgument(0));

            Stop result = stopService.updateStopByName(oldName, command);

            assertThat(result.getName()).isEqualTo(newName);
            assertThat(result.getCoordinates().getLatitude()).isEqualTo(newLat);
            assertThat(result.getCoordinates().getLongitude()).isEqualTo(newLon);

            verify(stopRepository).findByName(oldName);
            verify(stopRepository).save(any(StopEntity.class));
        }

        @Test
        void updateStopByName_StopNotExists_ThrowException() {
            FullUpdateStopCommand command = new FullUpdateStopCommand("Any Name",
                new FullUpdateStopCommand.FullUpdateGeographicCommand(LAT, LON));

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> stopService.updateStopByName(STOP_NAME, command));

            verify(stopRepository, never()).save(any());
        }
    }

    @Nested
    class StopRemoval {
        @Test
        void deleteStopByName() {
            when(stopRepository.existsByName(STOP_NAME)).thenReturn(true);
            when(routeRepository.existsByStop(STOP_NAME)).thenReturn(false);

            stopService.deleteStopByName(STOP_NAME);

            verify(stopRepository).deleteByName(STOP_NAME);
        }

        @Test
        void deleteStopByName_StopNotExists_ThrowException() {
            when(stopRepository.existsByName(STOP_NAME)).thenReturn(false);

            assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> stopService.deleteStopByName(STOP_NAME));

            verify(stopRepository, never()).deleteByName(anyString());
        }

        @Test
        void deleteStopByName_RouteContainStop_ThrowException() {
            when(stopRepository.existsByName(STOP_NAME)).thenReturn(true);
            when(routeRepository.existsByStop(STOP_NAME)).thenReturn(true);

            assertThatExceptionOfType(InUseException.class).isThrownBy(
                () -> stopService.deleteStopByName(STOP_NAME));

            verify(stopRepository, never()).deleteByName(anyString());
        }
    }

    private CreateStopCommand createCreateCommand() {
        return new CreateStopCommand(STOP_NAME,
            new CreateStopCommand.CreateGeographicCoordinatesCommand(LAT, LON));
    }

    private StopEntity createStopEntity() {
        return StopEntity.builder().id(UUID.randomUUID()).name(StopServiceImplTest.STOP_NAME)
            .geographicCoordinates(
                GeographicCoordinatesEntity.builder().id(UUID.randomUUID()).latitude(LAT)
                    .longitude(LON).build()).build();
    }
}
