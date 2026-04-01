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
    private static final Stop stop1 = Stop.builder().name("Stop1").coordinates(
            Stop.GeographicCoordinates.builder().latitude(53.198050).longitude(50.108750).build())
        .build();
    private static final Stop stop2 = Stop.builder().name("Stop2").coordinates(
            Stop.GeographicCoordinates.builder().latitude(53.195873).longitude(50.104954).build())
        .build();
    private List<RouteStop> stopsRoute;
    private BusinessHours businessHours;
    private Route route;


    @BeforeEach
    void setUp() {
        stopsRoute =
            List.of(RouteStop.builder().arriveAtFromStart(0).stopOrder(1).stop(stop1).build(),
                RouteStop.builder().arriveAtFromStart(120).stopOrder(2).stop(stop2).build());
        businessHours =
            BusinessHours.builder().startAt(LocalTime.of(5, 30)).endAt(LocalTime.of(23, 0)).build();
        route = Route.builder().name("route1").type("bus").stops(stopsRoute)
            .interval(Duration.ofMinutes(10)).businessHours(businessHours).build();
    }

    @ParameterizedTest
    @MethodSource("provideValueOf")
    void valueOf(String name, String type, Iterable<RouteStop> stops, Duration interval,
                 BusinessHours businessHours, String expectedFieldNameInException) {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> Route.builder().name(name).type(type).stops(stops).interval(interval)
                    .businessHours(businessHours).build())
            .withMessageContaining(expectedFieldNameInException);
    }

    private static Stream<Arguments> provideValueOf() {
        RouteStop s1 = RouteStop.builder().arriveAtFromStart(0).stopOrder(0).stop(stop1).build();
        RouteStop s2 = RouteStop.builder().arriveAtFromStart(15).stopOrder(1).stop(stop2).build();
        List<RouteStop> validStops = List.of(s1, s2);

        Duration validInterval = Duration.ofMinutes(15);
        BusinessHours validHours =
            BusinessHours.builder().startAt(LocalTime.of(9, 0)).endAt(LocalTime.of(17, 0)).build();

        return Stream.of(Arguments.of(null, "Bus", validStops, validInterval, validHours, "Name"),
            Arguments.of("", "Bus", validStops, validInterval, validHours, "Name"),

            Arguments.of("Line 1", null, validStops, validInterval, validHours, "Type"),
            Arguments.of("Line 1", "", validStops, validInterval, validHours, "Type"),

            Arguments.of("Line 1", "Bus", validStops, null, validHours, "Interval"),
            Arguments.of("Line 1", "Bus", validStops, Duration.ofMinutes(-1), validHours,
                "Interval"),

            Arguments.of("Line 1", "Bus", List.of(s1), validInterval, validHours, "Stops"),

            Arguments.of("Line 1", "Bus", Collections.emptyList(), validInterval, validHours,
                "Stops"));
    }

    @Test
    void addStop_ReturnNewStop() {
        Stop stop3 = Stop.builder().name("Stop3").coordinates(
                Stop.GeographicCoordinates.builder().latitude(53.198050).longitude(50.108750).build())
            .build();
        RouteStop routeStop3 =
            RouteStop.builder().arriveAtFromStart(240).stopOrder(3).stop(stop3).build();

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
    void deleteStop() {
        Stop stop3 = Stop.builder().name("Stop3").coordinates(
                Stop.GeographicCoordinates.builder().latitude(54.198050).longitude(51.108750).build())
            .build();
        RouteStop s3 = RouteStop.builder().arriveAtFromStart(240).stopOrder(3).stop(stop3).build();
        Route longRoute = Route.builder()
            .name("route1")
            .type("bus")
            .stops(List.of(stopsRoute.get(0), stopsRoute.get(1), s3))
            .interval(Duration.ofMinutes(10))
            .businessHours(businessHours).build();

        Route updatedRoute = longRoute.deleteStop(stopsRoute.get(0));

        assertThat(updatedRoute.getStopsCount()).isEqualTo(2);
        assertThat(updatedRoute.getStops()).extracting("stop").extracting("name")
            .containsExactly("Stop2", "Stop3");
    }

    @Test
    void deleteStop_ResultingInTooFewStops_ThrowsException() {
        RouteStop routeStop = stopsRoute.get(0);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
            () -> route.deleteStop(routeStop));
    }

    @Test
    void reverseRoute_WithMultipleStops() {
        Stop stop3 = Stop.builder()
            .name("Stop3")
            .coordinates(
                Stop.GeographicCoordinates.builder().latitude(54.198050).longitude(51.108750)
                    .build())
            .build();
        RouteStop s1 = RouteStop.builder().arriveAtFromStart(0).stopOrder(1).stop(stop1).build();
        RouteStop s2 =
            RouteStop.builder().arriveAtFromStart(10).stopOrder(2).stop(stop2).build();
        RouteStop s3 =
            RouteStop.builder().arriveAtFromStart(25).stopOrder(3).stop(stop3).build();

        Route route2 = Route.builder()
            .name("Route")
            .type("bus")
            .stops(List.of(s1, s2, s3))
            .interval(Duration.ofMinutes(10))
            .businessHours(businessHours).build();

        Route reversed = route2.reverseRoute();

        assertThat(reversed.getStops()).extracting("arriveAtFromStart", "stopOrder")
            .containsExactly(tuple(0, 1), tuple(15, 2), tuple(25, 3));
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
        assertThat(route.getStops()).hasSize(2).containsExactlyElementsOf(stopsRoute);
    }
}
