package ru.teamscore.busroutes.model.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Builder(builderClassName = "StopBuilder")
public class Stop {
    private final String name;
    private final GeographicCoordinates coordinates;

    private Stop(String name, GeographicCoordinates coordinates) {
        this.name = name;
        this.coordinates = new GeographicCoordinates(coordinates.latitude, coordinates.longitude);
    }

    @JsonCreator
    public static Stop valueOf(String name, GeographicCoordinates coordinates) {
        return builder().name(name).coordinates(coordinates).build();
    }

    public static class StopBuilder {
        public Stop build() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Stop name cannot be null or blank");
            }
            if (coordinates == null) {
                throw new IllegalArgumentException("Coordinates cannot be null");
            }
            return new Stop(name, coordinates);
        }
    }

    @Getter
    @EqualsAndHashCode
    @Builder(builderClassName = "GeographicCoordinatesBuilder")
    public static class GeographicCoordinates {
        private final double latitude;
        private final double longitude;

        private GeographicCoordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @JsonCreator
        public static GeographicCoordinates valueOf(double latitude, double longitude) {
            return builder().latitude(latitude).longitude(longitude).build();
        }

        public static class GeographicCoordinatesBuilder {
            public GeographicCoordinates build() {
                if (latitude < -90 || latitude > 90) {
                    throw new IllegalArgumentException("Latitude out of range [-90, 90]");
                }
                if (longitude < -180 || longitude > 180) {
                    throw new IllegalArgumentException("Longitude out of range [-180, 180]");
                }
                return new GeographicCoordinates(latitude, longitude);
            }
        }
    }
}
