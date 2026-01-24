package ru.teamscore.busroutes.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.teamscore.busroutes.data.entities.RouteEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, UUID> {

    boolean existsByName(String name);

    Optional<RouteEntity> findByName(String name);

    @Query("select r from RouteEntity r join r.stops rs where rs = :stopName")
    List<RouteEntity> findRoutesByStop(String stopName);

    @Query("select r from RouteEntity r join r.stops rs1 join r.stops rs2 where rs1.stop.name = " +
        ":stop1 and rs2.stop.name = :stop2")
    List<RouteEntity> findRoutesByBothStops(@Param("stop1") String stop1,
                                            @Param("stop2") String stop2);
    @Modifying
    void deleteByName(String name);

}
