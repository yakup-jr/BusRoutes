package ru.teamscore.busroutes.model.models;

import lombok.Getter;
import lombok.NonNull;

import java.time.Duration;
import java.time.LocalTime;

@Getter
public class Travel {

    private final Route route;
    private final Duration timeInRoute;
    private final LocalTime nextArrival;

    private Travel(Route route, Duration timeInRoute, LocalTime nextArrival) {
        this.route = route;
        this.timeInRoute = timeInRoute;
        this.nextArrival = nextArrival;
    }

    public static Travel valueOf(@NonNull Route route,
                                 @NonNull Duration timeInRoute,
                                 @NonNull LocalTime nextArrival) {
        if (timeInRoute.isNegative()) {
            throw new IllegalArgumentException("Time in route must be non-negative");
        }

        return new Travel(route, timeInRoute, nextArrival);
    }

}
