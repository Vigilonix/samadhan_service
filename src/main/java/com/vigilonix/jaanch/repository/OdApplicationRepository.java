package com.vigilonix.jaanch.repository;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.pojo.ODApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OdApplicationRepository extends JpaRepository<OdApplication, Long> {

    OdApplication findByUuid(UUID uuid);

    List<OdApplication> findByOdUuid(UUID uuid);

    List<OdApplication> findByFieldGeoNodeUuidIn(List<UUID> uuids);

    List<OdApplication> findByOdUuidAndStatus(UUID uuid, ODApplicationStatus status);

    List<OdApplication> findByFieldGeoNodeUuidInAndStatus(List<UUID> uuid, ODApplicationStatus status);
}