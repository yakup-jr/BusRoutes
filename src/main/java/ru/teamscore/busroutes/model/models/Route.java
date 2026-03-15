package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.teamscore.busroutes.model.enums.ItemType;
import ru.teamscore.busroutes.model.exceptions.NotFoundException;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Getter
@EqualsAndHashCode
public class Route {

    private final UUID id;
    private final String name;
    private final String type;
    private final List<RouteStop> stops;
    private final Duration interval;
    private final BusinessHours businessHours;

    @Builder
    private Route(UUID id, String name, String type, Iterable<RouteStop> stops, Duration interval,
                  BusinessHours businessHours) {
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
        if (stops == null || StreamSupport.stream(stops.spliterator(), false).toList().size() < 2) {
            throw new IllegalArgumentException("Stops must contain at least 2 elements");
        }

        this.id = id;
        this.name = name;
        this.type = type;
        this.stops = List.copyOf(StreamSupport.stream(stops.spliterator(), false).toList());
        this.interval = interval;
        this.businessHours = businessHours;
    }

    @JsonCreator
    public static Route valueOf(UUID id, String name, String type, Iterable<RouteStop> stops,
                                Duration interval, BusinessHours businessHours) {
        List<RouteStop> routeStops = StreamSupport.stream(stops.spliterator(), false).toList();
        return new Route(id, name, type, routeStops, interval, businessHours);
    }

    public Route addStop(RouteStop stop) {
        if (stops.contains(stop)) {
            throw new IllegalArgumentException(
                String.format("Stop with name %s already exists", stop.getStop().getName()));
        }

        List<RouteStop> routeStops = new ArrayList<>(this.stops);
        routeStops.add(stop);
        return Route.valueOf(id, name, type, routeStops, interval, businessHours);
    }

    public Travel createTravelToLastStop(String stopName, Clock clock) {
        int lastStopSeconds = stops.stream()
            .mapToInt(RouteStop::getArriveAtFromStart)
            .max()
            .orElse(0);
        return createTravel(stopName, lastStopSeconds, clock);
    }

    public Optional<Travel> createTravelBetweenStops(String fromStopName, String toStopName,
                                                     Clock clock) {
        int toSeconds = findStopByName(toStopName).getArriveAtFromStart();
        Travel travel = createTravel(fromStopName, toSeconds, clock);

        if (travel.getTimeInRoute().isZero()) {
            return Optional.empty();
        }
        return Optional.of(travel);
    }

    private Travel createTravel(String fromStopName, int toSeconds, Clock clock) {
        int fromSeconds = findStopByName(fromStopName).getArriveAtFromStart();
        int diffSeconds = Math.max(0, toSeconds - fromSeconds);

        return calculateTravelMetrics(Duration.ofSeconds(diffSeconds), clock);
    }

    private RouteStop findStopByName(String name) {
        return stops.stream()
            .filter(rs -> rs.getStop().getName().equals(name))
            .findFirst()
            .orElseThrow(() -> new NotFoundException(name, ItemType.STOP));
    }

    private Travel calculateTravelMetrics(Duration timeInRoute, Clock clock) {
        LocalTime now = LocalTime.now(clock);
        LocalTime startTime = businessHours.getStartAt();
        LocalTime endTime = businessHours.getEndAt();

        LocalTime nextArrival;
        if (now.isBefore(startTime) || now.isAfter(endTime)) {
            nextArrival = startTime;
        } else {
            nextArrival = calculateNextArriveAt(now, startTime);
        }

        return Travel.valueOf(this, timeInRoute, nextArrival);
    }

    private LocalTime calculateNextArriveAt(LocalTime now, LocalTime startTime) {
        long secondsSinceStart = Duration.between(startTime, now).getSeconds();
        long intervalSec = interval.getSeconds();

        long nextIntervalIdx = (secondsSinceStart / intervalSec) + 1;
        return startTime.plus(interval.multipliedBy(nextIntervalIdx));
    }

    public Route deleteStop(RouteStop stop) {
        List<RouteStop> routeStops = new ArrayList<>(this.stops);
        routeStops.remove(stop);
        return Route.valueOf(id, name, type, routeStops, interval, businessHours);
    }

    public Route reverseRoute() {
        List<RouteStop> reverseStops = new ArrayList<>();
        int i = stops.size() - 1;
        int j = 0;
        while (i >= 0) {
            int arriveAtFromStart = stops.get(j).getArriveAtFromStart();
            int order = stops.get(j).getStopOrder();
            Stop stop = stops.get(i).getStop();
            reverseStops.add(RouteStop.valueOf(null, arriveAtFromStart, order, stop));

            i--;
            j++;
        }

        return Route.valueOf(id, name, type, reverseStops, interval, businessHours);
    }

    public Route copy() {
        return Route.valueOf(id, String.format("%s_copy", name), type, stops, interval,
            businessHours.copy());
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
}
