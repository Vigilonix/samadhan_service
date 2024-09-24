package com.vigilonix.jaanch.repository;

import com.vigilonix.jaanch.aop.Timed;
import com.vigilonix.jaanch.model.Kand;
import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.OdApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KandRepository extends JpaRepository<Kand, Long> {

    @Timed
    Kand findByUuid(UUID uuid);

    @Timed
    List<Kand> findByTargetGeoHierarchyNodeUuidIn(List<UUID> geoHierarchyNodeUuids);
}