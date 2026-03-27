package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
public class RouteStop {
    private final UUID id;
    private final int arriveAtFromStart;
    private final int stopOrder;
    private final Stop stop;

    @Builder
    private RouteStop(UUID id, int arriveAtFromStart, int stopOrder, Stop stop) {
        if (arriveAtFromStart < 0) {
            throw new IllegalArgumentException("Arrive at from start must be non-negative");
        }
        if (stopOrder < 0) {
            throw new IllegalArgumentException("Order must be non-negative");
        }
        if (stop == null) {
            throw new IllegalArgumentException("Stop cannot be null");
        }

        this.id = id;
        this.arriveAtFromStart = arriveAtFromStart;
        this.stopOrder = stopOrder;
        this.stop = stop;
    }

    @JsonCreator
    public static RouteStop valueOf(UUID id, int arriveAtFromStart, int stopOrder, Stop stop) {
        return new RouteStop(id, arriveAtFromStart, stopOrder, stop);
    }
}
