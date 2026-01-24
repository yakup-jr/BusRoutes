package ru.teamscore.busroutes.data.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "t_route_stop", schema = "transport")
public class RouteStopEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    private int arriveAtFromStart;

    private int stopOrder;

    @OneToOne
    @JoinColumn(name = "stop_id", referencedColumnName = "id")
    private StopEntity stop;

    @ManyToOne
    @JoinColumn(name = "route_id", referencedColumnName = "id")
    private RouteEntity route;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof RouteStopEntity that)) return false;

        return arriveAtFromStart == that.arriveAtFromStart && stopOrder == that.stopOrder &&
            Objects.equals(id, that.id) && Objects.equals(stop, that.stop) &&
            Objects.equals(route, that.route);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + arriveAtFromStart;
        result = 31 * result + stopOrder;
        result = 31 * result + Objects.hashCode(stop);
        result = 31 * result + Objects.hashCode(route);
        return result;
    }
}
