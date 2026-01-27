package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalTime;

@Getter
@EqualsAndHashCode
public class Travel {

    private final Route route;
    private final Duration timeInRoute;
    private final LocalTime nextArrival;

    @Builder
    private Travel(Route route, Duration timeInRoute, LocalTime nextArrival) {
        if (route == null) {
            throw new IllegalArgumentException("Route must be non null");
        }
        if (timeInRoute.isNegative()) {
            throw new IllegalArgumentException("Time in route must be non-negative");
        }
        if (nextArrival == null) {
            throw new IllegalArgumentException("Next arrival must be non null");
        }

        this.route = route;
        this.timeInRoute = timeInRoute;
        this.nextArrival = nextArrival;
    }

    @JsonCreator
    public static Travel valueOf(Route route, Duration timeInRoute, LocalTime nextArrival) {
        return new Travel(route, timeInRoute, nextArrival);
    }

}
