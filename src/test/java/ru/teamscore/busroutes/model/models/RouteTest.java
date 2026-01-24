package ru.teamscore.busroutes.model.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

class RouteTest {
    private static final Stop stop1 =
        Stop.valueOf("Stop1", Stop.GeographicCoordinates.valueOf(53.198050, 50.108750));
    private static final Stop stop2 =
        Stop.valueOf("Stop2", Stop.GeographicCoordinates.valueOf(53.195873, 50.104954));
    private List<RouteStop> stopsRoute;
    private BusinessHours businessHours;
    private Route route;


    @BeforeEach
    void setUp() {
        stopsRoute = List.of(RouteStop.valueOf(0, 1, stop1),
            RouteStop.valueOf(120, 2, stop2));
        businessHours =
            BusinessHours.valueOf(LocalTime.of(5, 30), LocalTime.of(23, 0));

        route = Route.valueOf("route1", "bus", stopsRoute, Duration.ofMinutes(10), businessHours);
    }

    @ParameterizedTest
    @MethodSource("provideValueOf")
    void valueOf(String name, String type, Iterable<RouteStop> stops,
                 Duration interval,
                 BusinessHours businessHours, String expectedFieldNameInException) {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> Route.valueOf(name,
                    type, stops, interval, businessHours))
            .withMessageContaining(expectedFieldNameInException);
    }

    private static Stream<Arguments> provideValueOf() {
        RouteStop s1 = RouteStop.valueOf(0, 0, stop1);
        RouteStop s2 = RouteStop.valueOf(15, 1, stop2);
        List<RouteStop> validStops = List.of(s1, s2);

        Duration validInterval = Duration.ofMinutes(15);
        BusinessHours validHours = BusinessHours.valueOf(
            LocalTime.of(9, 0), LocalTime.of(17, 0)
        );

        return Stream.of(
            Arguments.of(null, "Bus", validStops, validInterval, validHours, "Name"),
            Arguments.of("", "Bus", validStops, validInterval, validHours, "Name"),

            Arguments.of("Line 1", null, validStops, validInterval, validHours, "Type"),
            Arguments.of("Line 1", "", validStops, validInterval, validHours, "Type"),

            Arguments.of("Line 1", "Bus", validStops, null, validHours, "Interval"),
            Arguments.of("Line 1", "Bus", validStops, Duration.ofMinutes(-1), validHours,
                "Interval"),

            Arguments.of("Line 1", "Bus", List.of(s1), validInterval, validHours, "Stops"),

            Arguments.of("Line 1", "Bus", Collections.emptyList(), validInterval, validHours,
                "Stops")
        );
    }

    @Test
    void addStop_ReturnNewStop() {
        Stop stop3 = Stop.valueOf("Stop3", Stop.GeographicCoordinates.valueOf(53.198050,
            50.108750));
        RouteStop routeStop3 = RouteStop.valueOf(240, 3, stop3);

        Route updatedRoute = route.addStop(routeStop3);

        assertThat(updatedRoute.getStops()).element(2).isEqualTo(routeStop3);
    }

    @Test
    void addStop_StopAlreadyExists_ThrowException() {
        RouteStop routeStop = stopsRoute.get(0);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
            () -> route.addStop(routeStop));
    }

    @Test
    void removeStop() {
        Stop stop3 =
            Stop.valueOf("Stop3", Stop.GeographicCoordinates.valueOf(54.198050, 51.108750));
        RouteStop s3 = RouteStop.valueOf(240, 3, stop3);
        Route longRoute = Route.valueOf("route1", "bus",
            List.of(stopsRoute.get(0), stopsRoute.get(1), s3),
            Duration.ofMinutes(10), businessHours);

        Route updatedRoute = longRoute.removeStop(stopsRoute.get(0));

        assertThat(updatedRoute.getStopsCount()).isEqualTo(2);
        assertThat(updatedRoute.getStops())
            .extracting("stop").extracting("name")
            .containsExactly("Stop2", "Stop3");
    }

    @Test
    void removeStop_ResultingInTooFewStops_ThrowsException() {
        RouteStop routeStop = stopsRoute.get(0);
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> route.removeStop(routeStop));
    }

    @Test
    void reverseRoute_WithMultipleStops() {
        Stop stop3 =
            Stop.valueOf("Stop3", Stop.GeographicCoordinates.valueOf(54.198050, 51.108750));
        RouteStop s1 = RouteStop.valueOf(0, 1, stop1);
        RouteStop s2 = RouteStop.valueOf(10, 2, stop2);
        RouteStop s3 = RouteStop.valueOf(25, 3, stop3);

        Route route2 = Route.valueOf("Route", "bus", List.of(s1, s2, s3),
            Duration.ofMinutes(10), businessHours);

        Route reversed = route2.reverseRoute();

        assertThat(reversed.getStops())
            .extracting("arriveAtFromStart", "stopOrder")
            .containsExactly(
                tuple(0, 1),
                tuple(10, 2),
                tuple(25, 3)
            );
        assertThat(reversed.getStops()).extracting("stop").extracting("name")
            .containsExactly("Stop3", "Stop2", "Stop1");
    }

    @Test
    void getStopsCount() {
        assertThat(route.getStopsCount()).isEqualTo(2);
    }

    @Test
    void containsStop() {
        assertThat(route.containsStop("Stop1")).isTrue();
        assertThat(route.containsStop("Stop2")).isTrue();
        assertThat(route.containsStop("NonExistent")).isFalse();
    }

    @Test
    void getStops() {
        assertThat(route.getStops())
            .hasSize(2)
            .containsExactlyElementsOf(stopsRoute);
    }
}
