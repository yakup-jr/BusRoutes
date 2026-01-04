package ru.teamscore.busroutes.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;
import ru.teamscore.busroutes.model.models.Stop;
import ru.teamscore.busroutes.model.services.StopServiceImpl;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StopServiceImplTest {
    private StopServiceImpl stopService;

    @BeforeEach
    void setUp() {
        ArrayList<Stop> stops = new ArrayList<>();
        stopService = Mockito.spy(new StopServiceImpl(stops));
    }

    @Test
    void addStop() {
        Stop stop = Stop.valueOf("Ulyanovskaya Street", 53.198050, 50.108750);
        Stop newStop = stopService.addStop(stop);

        assertThat(newStop).isEqualTo(stop);
        assertThat(stopService.getStopByName("Ulyanovskaya Street")).isEqualTo(stop);
        verify(stopService, times(1)).addStop(stop);
    }

    @Test
    void getStopByName() {
        Stop stop = Stop.valueOf("Ulyanovskaya Street", 53.198050, 50.108750);
        stopService.addStop(stop);

        Stop foundStop = stopService.getStopByName("Ulyanovskaya Street");

        assertThat(foundStop).isEqualTo(stop);
        verify(stopService, times(1)).getStopByName("Ulyanovskaya Street");
    }

    @Test
    void updateStopByName() {
        Stop stop = Stop.valueOf("Ulyanovskaya Street", 53.198050, 50.108750);
        stopService.addStop(stop);

        Stop newStop = Stop.valueOf("Ulyanovskaya Street 2", 54.198050, 51.108750);
        Stop updatedStop = stopService.updateStopByName("Ulyanovskaya Street", newStop);

        assertThat(updatedStop).isEqualTo(newStop);
        verify(stopService, times(1)).updateStopByName("Ulyanovskaya Street", newStop);
    }

    @Test
    void removeStopByName() {
        Stop stop = Stop.valueOf("Ulyanovskaya Street", 53.198050, 50.108750);
        stopService.addStop(stop);

        stopService.removeStopByName("Ulyanovskaya Street");

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
            () -> stopService.getStopByName("Ulyanovskaya Street"));
        verify(stopService, times(1)).removeStopByName("Ulyanovskaya Street");
    }
}
