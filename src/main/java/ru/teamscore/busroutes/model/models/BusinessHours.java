package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@EqualsAndHashCode
@Builder(builderClassName = "BusinessHoursBuilder")
public class BusinessHours {

    private final LocalTime startAt;
    private final LocalTime endAt;

    private BusinessHours(LocalTime startAt, LocalTime endAt) {
        this.startAt = startAt;
        this.endAt = endAt;
    }

    @JsonCreator
    public static BusinessHours valueOf(LocalTime startAt, LocalTime endAt) {
        return builder().startAt(startAt).endAt(endAt).build();
    }

    public static class BusinessHoursBuilder {
        public BusinessHours build() {
            if (startAt == null) {
                throw new IllegalArgumentException("StartAt can't be null");
            }
            if (endAt == null) {
                throw new IllegalArgumentException("EndAt can't be null");
            }
            return new BusinessHours(this.startAt, this.endAt);
        }
    }
}
