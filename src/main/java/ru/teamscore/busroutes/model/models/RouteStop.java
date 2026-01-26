package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Builder
public class RouteStop {

    private final int arriveAtFromStart;
    private final int stopOrder;
    private final Stop stop;

    private RouteStop(int arriveAtFromStart, int stopOrder, Stop stop) {
        this.arriveAtFromStart = arriveAtFromStart;
        this.stopOrder = stopOrder;
        this.stop = stop;
    }

    @JsonCreator
    public static RouteStop valueOf(int arriveAtFromStart, int stopOrder, Stop stop) {
        if (arriveAtFromStart < 0) {
            throw new IllegalArgumentException("Arrive at from start must be non-negative");
        }
        if (stopOrder < 0) {
            throw new IllegalArgumentException("Order must be non-negative");
        }
        return new RouteStop(arriveAtFromStart, stopOrder, stop);
    }
}
