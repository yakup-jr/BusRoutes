package ru.teamscore.busroutes.model.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Stop {
    private final String name;
    private final GeographicCoordinates coordinates;

    private Stop(String name, double latitude, double longitude) {
        this.name = name;
        this.coordinates = new GeographicCoordinates(latitude, longitude);
    }

    public static Stop valueOf(String name, double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }

        return new Stop(name, latitude, longitude);
    }

    @Getter
    @EqualsAndHashCode
    private static class GeographicCoordinates {
        private final double latitude;
        private final double longitude;

        private GeographicCoordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public GeographicCoordinates valueOf(double latitude, double longitude) {
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
