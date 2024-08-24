package com.vigilonix.jaanch.repository;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.OdApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OdApplicationRepository extends JpaRepository<OdApplication, Long> {

    OdApplication findByUuid(UUID uuid);

    List<OdApplication> findByOd(User uuid);

    List<OdApplication> findByGeoHierarchyNodeUuidIn(List<UUID> uuids);

    List<OdApplication> findByOdAndStatus(User od, OdApplicationStatus status);

    List<OdApplication> findByGeoHierarchyNodeUuidInAndStatus(List<UUID> uuid, OdApplicationStatus status);

    @Query("SELECT o FROM od_application o WHERE (o.od = :user OR o.enquiryOfficer = :user)")
    List<OdApplication> findByOdOrEnquiryOfficer(User user);

    @Query("SELECT o FROM od_application o WHERE (o.od = :user OR o.enquiryOfficer = :user) AND o.status = :status")
    List<OdApplication> findByOdOrEnquiryOfficerAndStatus(User user, OdApplicationStatus status);



    @Query(value = "SELECT MAX(receipt_bucket_number) " +
            "FROM od_application " +
            "WHERE geo_hierarchy_node_uuid = :geoHierarchyNodeUuid " +
            "AND DATE_TRUNC('month', TO_TIMESTAMP(created_at / 1000)) = DATE_TRUNC('month', CURRENT_DATE)",
            nativeQuery = true)
    Optional<Integer> findMaxReceiptBucketNumberForCurrentMonth(@Param("geoHierarchyNodeUuid") UUID geoHierarchyNodeUuid);
}