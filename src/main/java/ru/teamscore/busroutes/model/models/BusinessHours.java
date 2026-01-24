package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalTime;
//todo: Failed to set up a Bean Validation provider: jakarta.validation.NoProviderFoundException:
// Unable to create a Configuration, because no Jakarta Validation provider could be found.
// Add a provider like Hibernate Validator (RI) to your classpath.
@Getter
@EqualsAndHashCode
public class BusinessHours {

    @NotNull
    private final LocalTime startAt;
    @NotNull
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
