package ru.teamscore.busroutes.model.models;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalTime;

@Getter
@EqualsAndHashCode
@Builder
public class Travel {

    private final Route route;
    private final Duration timeInRoute;
    private final LocalTime nextArrival;

    private Travel(Route route, Duration timeInRoute, LocalTime nextArrival) {
        this.route = route;
        this.timeInRoute = timeInRoute;
        this.nextArrival = nextArrival;
    }

    public static Travel valueOf(Route route,
                                 Duration timeInRoute,
                                 LocalTime nextArrival) {
        if (route == null) {
            throw new IllegalArgumentException("route can't be null");
        }
        if (timeInRoute.isNegative()) {
            throw new IllegalArgumentException("Time in route must be non-negative");
        }
        if (nextArrival == null) {
            throw new IllegalArgumentException("next arrival can't be null");
        }

        return new Travel(route, timeInRoute, nextArrival);
    }

}
