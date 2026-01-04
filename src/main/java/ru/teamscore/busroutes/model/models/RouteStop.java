package ru.teamscore.busroutes.model.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public class RouteStop {

    private final int arriveAtFromStart;
    private final int order;
    private final Stop stop;

    private RouteStop(int arriveAtFromStart, int order, Stop stop) {
        this.arriveAtFromStart = arriveAtFromStart;
        this.order = order;
        this.stop = stop;
    }

    public static RouteStop valueOf(int arriveAtFromStart, int order, Stop stop) {
        if (arriveAtFromStart < 0) {
            throw new IllegalArgumentException("Arrive at from start must be non-negative");
        }
        if (order < 0) {
            throw new IllegalArgumentException("Order must be non-negative");
        }
        return new RouteStop(arriveAtFromStart, order, stop);
    }
}
