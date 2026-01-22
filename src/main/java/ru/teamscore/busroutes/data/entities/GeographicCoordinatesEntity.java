package ru.teamscore.busroutes.data.entities;

import jakarta.persistence.*;
import lombok.*;

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
}
