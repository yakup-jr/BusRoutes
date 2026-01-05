package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Duration;
import java.util.List;

@Getter
@EqualsAndHashCode
public class Route {

    private final String name;
    private final String type;
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
    public static Route valueOf(String name, String type, List<RouteStop> stops, Duration interval,
                                BusinessHours businessHours) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        if (type.isEmpty()) {
            throw new IllegalArgumentException("Type must not be empty");
        }
        if (stops.size() < 2) {
            throw new IllegalArgumentException("Stops must contain at least 2 elements");
        }
        if (interval.isNegative()) {
            throw new IllegalArgumentException("Interval must be non-negative");
        }

        return new Route(name, type, stops, interval, businessHours);
    }
}
