package com.vigilonix.applicationnadministrativeservice.repository;

import com.vigilonix.applicationnadministrativeservice.aop.Timed;
import com.vigilonix.applicationnadministrativeservice.model.Kand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KandRepository extends JpaRepository<Kand, Long> {

    @Timed
    Kand findByUuid(UUID uuid);

    @Timed
    List<Kand> findByTargetGeoHierarchyNodeUuidIn(List<UUID> geoHierarchyNodeUuids);
}