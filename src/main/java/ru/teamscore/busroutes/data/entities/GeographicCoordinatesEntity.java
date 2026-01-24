package ru.teamscore.busroutes.data.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(schema = "transport", name = "t_geography_coordinates")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeographicCoordinatesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @OneToOne(mappedBy = "geographicCoordinates")
    private StopEntity stop;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof GeographicCoordinatesEntity that)) return false;

        return Double.compare(latitude, that.latitude) == 0 &&
            Double.compare(longitude, that.longitude) == 0 &&
            Objects.equals(id, that.id) && Objects.equals(stop, that.stop);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Double.hashCode(latitude);
        result = 31 * result + Double.hashCode(longitude);
        result = 31 * result + Objects.hashCode(stop);
        return result;
    }
}
