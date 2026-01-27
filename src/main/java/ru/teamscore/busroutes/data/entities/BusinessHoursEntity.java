package ru.teamscore.busroutes.data.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "t_business_hours", schema = "transport")
public class BusinessHoursEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private LocalTime startAt;

    @Column(nullable = false)
    private LocalTime endAt;

    @OneToOne(mappedBy = "businessHours")
    private RouteEntity route;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof BusinessHoursEntity that)) return false;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
