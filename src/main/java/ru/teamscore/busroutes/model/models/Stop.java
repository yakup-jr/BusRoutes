package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Builder
public class Stop {
    private final String name;
    private final GeographicCoordinates coordinates;

    private Stop(String name, GeographicCoordinates coordinates) {
        this.name = name;
        this.coordinates = new GeographicCoordinates(coordinates.latitude, coordinates.longitude);
    }

    @JsonCreator
    public static Stop valueOf(
        String name, GeographicCoordinates coordinates
    ) {
        return new Stop(name, coordinates);
    }

    @Getter
    @EqualsAndHashCode
    @Builder
    public static class GeographicCoordinates {
        private final double latitude;
        private final double longitude;

        private GeographicCoordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @JsonCreator
        public static GeographicCoordinates valueOf(double latitude, double longitude) {
            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException("Latitude out of range");
            }
            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Longitude out of range");
            }
            return new GeographicCoordinates(latitude, longitude);
        }
    }
}
