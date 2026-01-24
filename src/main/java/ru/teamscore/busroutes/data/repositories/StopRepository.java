package ru.teamscore.busroutes.data.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import ru.teamscore.busroutes.data.entities.StopEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StopRepository extends JpaRepository<StopEntity, UUID> {

    boolean existsByName(String name);

    Optional<StopEntity> findByName(String name);

    List<StopEntity> findAllByNameIn(Collection<String> names);

    @Modifying
    void deleteByName(String name);

}
