package ru.teamscore.busroutes.data.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "geographic_coordinates_id", referencedColumnName = "id")
    private GeographicCoordinatesEntity geographicCoordinates;

    @OneToMany(mappedBy = "stop")
    private List<RouteStopEntity> routeStop;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof StopEntity that)) return false;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
