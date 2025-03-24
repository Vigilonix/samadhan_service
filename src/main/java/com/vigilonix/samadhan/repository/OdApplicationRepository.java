package com.vigilonix.samadhan.repository;

import com.vigilonix.samadhan.aop.Timed;
import com.vigilonix.samadhan.enums.ApplicationCategory;
import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.enums.OdApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OdApplicationRepository extends JpaRepository<OdApplication, Long> {

    @Timed
    OdApplication findByUuid(UUID uuid);

    @Timed
    List<OdApplication> findByOd(User uuid);

    @Timed
    @Query(value = "SELECT DISTINCT o FROM od_application o LEFT JOIN OdApplicationAssignment oaa ON oaa.application = o WHERE (o.geoHierarchyNodeUuid IN :geoNodeUuids OR oaa.geoHierarchyNodeUuid IN :geoNodeUuids)")
    List<OdApplication> findByGeoHierarchyNodeUuidIn(@Param("geoNodeUuids")List<UUID> geoNodeUuids);

    @Timed
    @Query(value = "SELECT DISTINCT o FROM od_application o LEFT JOIN OdApplicationAssignment oaa ON oaa.application = o WHERE (o.geoHierarchyNodeUuid IN :geoNodeUuids OR oaa.geoHierarchyNodeUuid IN :geoNodeUuids) AND o.status = :status" )
    List<OdApplication> findByGeoHierarchyNodeUuidInAndStatus(@Param("geoNodeUuids")List<UUID> geoNodeUuids, OdApplicationStatus status);

    @Timed
    @Query("SELECT distinct o FROM od_application o left join OdApplicationAssignment oaa on oaa.application = o WHERE o.od = :user OR oaa.geoHierarchyNodeUuid IN :geoNodeUuids")
    List<OdApplication> findByOdOrEnquiryOfficer(User user, @Param("geoNodeUuids") List<UUID> geoNodeUuids);

    @Timed
    List<OdApplication> findByOdAndGeoHierarchyNodeUuidIn(User user, @Param("geoNodeUuids") List<UUID> geoNodeUuids);

    @Timed
    @Query("SELECT distinct o FROM od_application o left join OdApplicationAssignment oaa on oaa.application = o WHERE (o.od = :user OR oaa.geoHierarchyNodeUuid IN :geoNodeUuids) AND oaa.status = :status")
    List<OdApplication> findByOdOrEnquiryOfficerAndStatus(User user, OdApplicationStatus status, @Param("geoNodeUuids") List<UUID> geoNodeUuids);



    @Timed
    @Query(value = "SELECT MAX(receipt_bucket_number) " +
            "FROM od_application " +
            "WHERE geo_hierarchy_node_uuid = :geoHierarchyNodeUuid " +
            "AND DATE_TRUNC('year', TO_TIMESTAMP(created_at / 1000)) = DATE_TRUNC('year', CURRENT_DATE)",
            nativeQuery = true)
    Optional<Integer> findMaxReceiptBucketNumberForCurrentMonth(@Param("geoHierarchyNodeUuid") UUID geoHierarchyNodeUuid);


    @Timed
    @Query(value = "SELECT sq.status, COUNT(sq.status) FROM (SELECT DISTINCT COALESCE(oaa.status, o.status) AS status, o.uuid as application_uuid FROM od_application o LEFT JOIN OdApplicationAssignment oaa ON oaa.application = o WHERE oaa.geoHierarchyNodeUuid IN :geoNodeUuids OR o.geoHierarchyNodeUuid IN :geoNodeUuids) AS sq GROUP BY sq.status")
    List<Object[]> applicationStatusCountByAssignmentGeoFilter(@Param("geoNodeUuids") List<UUID> geoNodeUuids);

    @Timed
    @Query(value = "SELECT sq.status, COUNT(sq.status) FROM (SELECT DISTINCT COALESCE(oaa.status, o.status) AS status, o.uuid as application_uuid FROM od_application o LEFT JOIN OdApplicationAssignment oaa ON oaa.application = o WHERE oaa.geoHierarchyNodeUuid IN :geoNodeUuids) AS sq GROUP BY sq.status")
    List<Object[]>  applicationStatusCountBytGeoFilter(@Param("geoNodeUuids") List<UUID> geoNodeUuids);

    @Timed
    @Query("SELECT distinct o FROM od_application o WHERE (lower(receiptNo) like CONCAT('%', :searchTerm, '%') or lower(applicantPhoneNumber) like CONCAT('%', :searchTerm, '%') or lower(applicantName) like CONCAT('%', :searchTerm, '%')) AND o.category IN :categories AND o.geoHierarchyNodeUuid IN :geoNodeUuids AND o.status = :status")
    List<OdApplication> findByStatusAndCategoryInAndGeoHierarchyNodeUuidIn(@Param("status") OdApplicationStatus status, @Param("categories") List<ApplicationCategory> categories, @Param("geoNodeUuids") List<UUID> geoNodes, @Param("searchTerm") String searchTerm);

    @Timed
    @Query("SELECT distinct o FROM od_application o left join OdApplicationAssignment oaa on oaa.application = o WHERE (lower(receiptNo) like CONCAT('%', :searchTerm, '%') or lower(applicantPhoneNumber) like CONCAT('%', :searchTerm, '%') or lower(applicantName) like CONCAT('%', :searchTerm, '%')) AND o.category IN :categories AND o.geoHierarchyNodeUuid IN :geoNodeUuids AND oaa.status = :status")
    List<OdApplication> findByCategoryInAndAssignmentStatusAndGeoHierarchyNodeUuidIn(@Param("categories") List<ApplicationCategory> categories, @Param("geoNodeUuids")List<UUID> geoNodes, @Param("status")OdApplicationStatus status, @Param("searchTerm") String searchTerm);

    @Timed
    @Query("SELECT distinct o FROM od_application o left join OdApplicationAssignment oaa on oaa.application = o WHERE (lower(receiptNo) like CONCAT('%', :searchTerm, '%') or lower(applicantPhoneNumber) like CONCAT('%', :searchTerm, '%') or lower(applicantName)  like CONCAT('%', :searchTerm, '%')) AND o.category IN :categories AND oaa.geoHierarchyNodeUuid IN :geoNodeUuids AND oaa.status = :status")
    List<OdApplication> findByCategoryInAndAssignmentStatusAndAssignmentGeoHierarchyNodeUuidIn(@Param("categories") List<ApplicationCategory> categories, @Param("geoNodeUuids")List<UUID> geoNodes, @Param("status")OdApplicationStatus status, @Param("searchTerm")String searchTerm);

    @Timed
    @Query("SELECT distinct o FROM od_application o WHERE (lower(receiptNo) like CONCAT('%', :searchTerm, '%') or lower(applicantPhoneNumber) like CONCAT('%', :searchTerm, '%') or lower(applicantName) like CONCAT('%', :searchTerm, '%')) AND o.category IN :categories AND o.geoHierarchyNodeUuid IN :geoNodeUuids")
    List<OdApplication> findByCategoryInAndGeoHierarchyNodeUuidIn(@Param("categories") List<ApplicationCategory> categories, @Param("geoNodeUuids") List<UUID> geoNodes, @Param("searchTerm") String searchTerm);
}