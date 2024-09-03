package com.vigilonix.jaanch.repository;

import com.vigilonix.jaanch.aop.Timed;
import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.OdApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface OdApplicationRepository extends JpaRepository<OdApplication, Long> {

    @Timed
    OdApplication findByUuid(UUID uuid);

    @Timed
    List<OdApplication> findByOd(User uuid);

    @Timed
    List<OdApplication> findByGeoHierarchyNodeUuidIn(List<UUID> uuids);

    @Timed
    List<OdApplication> findByGeoHierarchyNodeUuidInAndStatus(List<UUID> uuid, OdApplicationStatus status);

    @Timed
    @Query("SELECT o FROM od_application o WHERE (o.od = :user OR o.enquiryOfficer = :user)")
    List<OdApplication> findByOdOrEnquiryOfficer(User user);

    @Timed
    @Query("SELECT o FROM od_application o WHERE (o.od = :user OR o.enquiryOfficer = :user) AND o.status = :status")
    List<OdApplication> findByOdOrEnquiryOfficerAndStatus(User user, OdApplicationStatus status);



    @Timed
    @Query(value = "SELECT MAX(receipt_bucket_number) " +
            "FROM od_application " +
            "WHERE geo_hierarchy_node_uuid = :geoHierarchyNodeUuid " +
            "AND DATE_TRUNC('year', TO_TIMESTAMP(created_at / 1000)) = DATE_TRUNC('year', CURRENT_DATE)",
            nativeQuery = true)
    Optional<Integer> findMaxReceiptBucketNumberForCurrentMonth(@Param("geoHierarchyNodeUuid") UUID geoHierarchyNodeUuid);


    @Timed
    @Query("SELECT o.status, COUNT(o) FROM od_application o WHERE o.geoHierarchyNodeUuid IN :geoNodeUuids GROUP BY o.status")
    List<Object[]> countByStatusForGeoNodes(@Param("geoNodeUuids") List<UUID> geoNodeUuids);

    @Timed
    @Query("SELECT o.status, COUNT(o) FROM od_application o WHERE o.enquiryOfficer = :user  GROUP BY o.status")
    List<Object[]>  countByStatusForOdOfficer(User user);
}