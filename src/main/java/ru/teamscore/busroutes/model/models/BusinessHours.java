package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@EqualsAndHashCode
public class BusinessHours {
    private final UUID id;
    private final LocalTime startAt;
    private final LocalTime endAt;

    @Builder
    private BusinessHours(UUID id, LocalTime startAt, LocalTime endAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("StartAt can't be null");
        }
        if (endAt == null) {
            throw new IllegalArgumentException("EndAt can't be null");
        }

        this.id = id;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    @JsonCreator
    public static BusinessHours valueOf(UUID id, LocalTime startAt, LocalTime endAt) {
        return new BusinessHours(id, startAt, endAt);
    }

    public BusinessHours copy() {
        return BusinessHours.builder().id(UUID.randomUUID()).startAt(startAt).endAt(endAt).build();
    }
}
