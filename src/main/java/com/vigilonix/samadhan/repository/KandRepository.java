package com.vigilonix.samadhan.repository;

import com.vigilonix.samadhan.aop.Timed;
import com.vigilonix.samadhan.model.Kand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface KandRepository extends JpaRepository<Kand, Long> {

    @Timed
    Kand findByUuid(UUID uuid);

    @Timed
    List<Kand> findByTargetGeoHierarchyNodeUuidIn(List<UUID> geoHierarchyNodeUuids);
}