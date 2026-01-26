package ru.teamscore.busroutes.data.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_route", schema = "transport")
public class RouteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Duration interval;

    @OneToOne
    @JoinColumn(name = "business_hours_id", referencedColumnName = "id")
    private BusinessHoursEntity businessHours;

    @OneToMany(mappedBy = "route")
    private List<RouteStopEntity> stops;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof RouteEntity that)) return false;

        return Objects.equals(id, that.id) && name.equals(that.name) &&
            type.equals(that.type) && interval.equals(that.interval) &&
            Objects.equals(businessHours, that.businessHours) &&
            Objects.equals(stops, that.stops);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + interval.hashCode();
        result = 31 * result + Objects.hashCode(businessHours);
        result = 31 * result + Objects.hashCode(stops);
        return result;
    }
}
