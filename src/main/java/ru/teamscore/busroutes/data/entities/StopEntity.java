package ru.teamscore.busroutes.data.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(schema = "transport", name = "t_stop")
public class StopEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToOne
    @JoinColumn(name = "geographic_coordinates_id", referencedColumnName = "id")
    private GeographicCoordinatesEntity geographicCoordinates;

    @OneToOne(mappedBy = "stop")
    private RouteStopEntity routeStop;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof StopEntity that)) return false;

        return Objects.equals(id, that.id) && name.equals(that.name) &&
            geographicCoordinates.equals(that.geographicCoordinates) &&
            Objects.equals(routeStop, that.routeStop);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + name.hashCode();
        result = 31 * result + geographicCoordinates.hashCode();
        result = 31 * result + Objects.hashCode(routeStop);
        return result;
    }
}
