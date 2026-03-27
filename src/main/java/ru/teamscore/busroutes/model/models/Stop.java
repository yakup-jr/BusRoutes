package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@Getter
@EqualsAndHashCode
public class Stop {
    private final UUID id;
    private final String name;
    private final GeographicCoordinates coordinates;

    @Builder
    private Stop(UUID id, String name, GeographicCoordinates coordinates) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Stop name cannot be null or blank");
        }
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }

        this.id = id;
        this.name = name;
        this.coordinates = new GeographicCoordinates(coordinates.latitude, coordinates.longitude);
    }

    @JsonCreator
    public static Stop valueOf(UUID id, String name, GeographicCoordinates coordinates) {
        return new Stop(id, name, coordinates);
    }

    @Getter
    @EqualsAndHashCode
    public static class GeographicCoordinates {
        private final double latitude;
        private final double longitude;

        @Builder
        private GeographicCoordinates(double latitude, double longitude) {
            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException("Latitude out of range [-90, 90]");
            }
            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Longitude out of range [-180, 180]");
            }

            this.latitude = latitude;
            this.longitude = longitude;
        }

        @JsonCreator
        public static GeographicCoordinates valueOf(double latitude, double longitude) {
            return builder().latitude(latitude).longitude(longitude).build();
        }
    }
}
