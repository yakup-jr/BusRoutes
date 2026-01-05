package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class BusinessHours {

    private final LocalTime startAt;
    private final LocalTime endAt;

    private BusinessHours(LocalTime startAt, LocalTime endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }

    @JsonCreator
    public static BusinessHours valueOf(LocalTime startAt, LocalTime endAt) {
        return new BusinessHours(startAt, endAt);
    }
}
