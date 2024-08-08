package com.vigilonix.jaanch.repository;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.ODApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OdApplicationRepository extends JpaRepository<OdApplication, Long> {

    OdApplication findByUuid(UUID uuid);

    List<OdApplication> findByOd(User uuid);

    List<OdApplication> findByFieldGeoNodeUuidIn(List<UUID> uuids);

    List<OdApplication> findByOdAndStatus(User od, ODApplicationStatus status);

    List<OdApplication> findByFieldGeoNodeUuidInAndStatus(List<UUID> uuid, ODApplicationStatus status);

    @Query("SELECT o FROM od_application o WHERE (o.od = :user OR o.enquiryOfficer = :user)")
    List<OdApplication> findByOdOrEnquiryOfficer(User user);

    @Query("SELECT o FROM od_application o WHERE (o.od = :user OR o.enquiryOfficer = :user) AND o.status = :status")
    List<OdApplication> findByOdOrEnquiryOfficerAndStatus(User user, ODApplicationStatus status);
}