package ru.teamscore.busroutes.data.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.teamscore.busroutes.data.entities.StopEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StopRepository extends JpaRepository<StopEntity, UUID>,
    PagingAndSortingRepository<StopEntity, UUID> {

    boolean existsByName(String name);

    Optional<StopEntity> findByName(String name);

    List<StopEntity> findAllByNameIn(Collection<String> names);

    Page<StopEntity> findByNameStartingWithIgnoreCase(String name,
                                                      Pageable pageable);

    long countByNameIn(Collection<String> names);

    @Modifying
    void deleteByName(String name);

}
