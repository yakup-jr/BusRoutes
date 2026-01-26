package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalTime;
@Getter
@EqualsAndHashCode
@Builder
public class BusinessHours {

    private final LocalTime startAt;
    private final LocalTime endAt;

    private BusinessHours(LocalTime startAt, LocalTime endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }

    @JsonCreator
    public static BusinessHours valueOf(LocalTime startAt, LocalTime endAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("start can't be null");
        }
        if (endAt == null) {
            throw new IllegalArgumentException("end at can't be null")
        }
        return new BusinessHours(startAt, endAt);
    }
}
