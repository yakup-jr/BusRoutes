package ru.teamscore.busroutes.data.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.teamscore.busroutes.data.entities.RouteEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, UUID>,
    PagingAndSortingRepository<RouteEntity, UUID> {

    boolean existsByName(String name);

    Iterable<RouteEntity> findByName(String name);

    //todo: not override findAll. Should be findAllEager
    @EntityGraph(attributePaths = {"stops", "stops.stop", "stops.stop.geographicCoordinates",
        "businessHours"})
    Page<RouteEntity> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"stops", "stops.stop", "businessHours", "stops.stop" +
        ".geographicCoordinates"})
    @Query("select r from RouteEntity r where r.id = :routeId")
    Optional<RouteEntity> findByIdEager(UUID routeId);

    @EntityGraph(attributePaths = {"stops", "stops.stop"})
    @Query("select r from RouteEntity r where r.name = :name")
    Iterable<RouteEntity> findByNameWithStops(String name);

    @Query("select r from RouteEntity r join r.stops rs where rs.stop.name = :stopName")
    List<RouteEntity> findRoutesByStop(String stopName);

    @Query("select count(r) > 0 from RouteEntity r join r.stops rs where rs.stop.name = :stopName")
    boolean existsByStop(String stopName);

    @Query("select r from RouteEntity r join r.stops rs1 join r.stops rs2 where rs1.stop.name = " +
        ":stop1 and rs2.stop.name = :stop2")
    List<RouteEntity> findRoutesByBothStops(@Param("stop1") String stop1,
                                            @Param("stop2") String stop2);

    @Modifying
    void deleteByName(String name);
}
