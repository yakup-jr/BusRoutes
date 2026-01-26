package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Getter
@EqualsAndHashCode
@Builder(builderClassName = "RouteBuilder")
public class Route {

    private final String name;
    private final String type;
    @Getter(AccessLevel.NONE)
    private final List<RouteStop> stops;
    private final Duration interval;
    private final BusinessHours businessHours;

    private Route(String name, String type, List<RouteStop> stops, Duration interval,
                  BusinessHours businessHours) {
        this.name = name;
        this.type = type;
        this.stops = List.copyOf(stops);
        this.interval = interval;
        this.businessHours = businessHours;
    }

    @JsonCreator
    public static Route valueOf(String name, String type, Iterable<RouteStop> stops,
                                Duration interval, BusinessHours businessHours) {
        return builder().name(name).type(type)
            .stops(stops != null ? StreamSupport.stream(stops.spliterator(), false).toList() : null)
            .interval(interval).businessHours(businessHours).build();
    }

    public Route addStop(RouteStop stop) {
        if (stops.contains(stop)) {
            throw new IllegalArgumentException(
                String.format("Stop with name %s already exists", stop.getStop().getName()));
        }

        List<RouteStop> routeStops = new ArrayList<>(this.stops);
        routeStops.add(stop);
        return Route.valueOf(name, type, routeStops, interval, businessHours);
    }

    public Route removeStop(RouteStop stop) {
        List<RouteStop> routeStops = new ArrayList<>(this.stops);
        routeStops.remove(stop);
        return Route.valueOf(name, type, routeStops, interval, businessHours);
    }

    public Route reverseRoute() {
        List<RouteStop> reverseStops = new ArrayList<>();
        int i = stops.size() - 1;
        int j = 0;
        while (i >= 0) {
            int arriveAtFromStart = stops.get(j).getArriveAtFromStart();
            int order = stops.get(j).getStopOrder();
            Stop stop = stops.get(i).getStop();
            reverseStops.add(RouteStop.valueOf(arriveAtFromStart, order, stop));

            i--;
            j++;
        }

        return Route.valueOf(name, type, reverseStops, interval, businessHours);
    }

    public Route copy() {
        return Route.valueOf(String.format("%s_copy", name), type, stops, interval, businessHours);
    }

    public int getStopsCount() {
        return stops.size();
    }

    public boolean containsStop(String stopName) {
        return stops.stream().anyMatch(stop -> stop.getStop().getName().equals(stopName));
    }

    public Iterable<RouteStop> getStops() {
        return stops;
    }

    public static class RouteBuilder {
        public Route build() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Name must not be empty");
            }
            if (type == null || type.isBlank()) {
                throw new IllegalArgumentException("Type must not be empty");
            }
            if (interval == null || interval.isNegative()) {
                throw new IllegalArgumentException("Interval must be non-negative");
            }
            if (businessHours == null) {
                throw new IllegalArgumentException("Business hours must not be null");
            }
            if (stops == null || stops.size() < 2) {
                throw new IllegalArgumentException("Stops must contain at least 2 elements");
            }
            return new Route(name, type, stops, interval, businessHours);
        }
    }
}
