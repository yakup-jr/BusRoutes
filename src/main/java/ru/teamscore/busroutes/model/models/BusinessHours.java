package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@EqualsAndHashCode
public class BusinessHours {
    private final LocalTime startAt;
    private final LocalTime endAt;

    @Builder
    private BusinessHours(LocalTime startAt, LocalTime endAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("StartAt can't be null");
        }
        if (endAt == null) {
            throw new IllegalArgumentException("EndAt can't be null");
        }

        this.startAt = startAt;
        this.endAt = endAt;
    }

    @JsonCreator
    public static BusinessHours valueOf(LocalTime startAt, LocalTime endAt) {
        return new BusinessHours(startAt, endAt);
    }
}
