package com.vigilonix.samadhan.repository;

import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.model.OdApplicationAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OdApplicationAssignmentRepository extends JpaRepository<OdApplicationAssignment, UUID> {

    // Finds an assignment by its UUID
    OdApplicationAssignment findByUuid(UUID uuid);

    // Finds all assignments for a specific application
    List<OdApplicationAssignment> findByApplication(OdApplication application);

    // Finds the latest assignment for each assignee within a specific application
    @Query("SELECT a FROM OdApplicationAssignment a WHERE a.application = :application AND a.createdAt IN (SELECT MAX(b.createdAt) FROM OdApplicationAssignment b WHERE b.enquiryOfficer = a.enquiryOfficer AND b.application = :application GROUP BY b.enquiryOfficer)")
    List<OdApplicationAssignment> findLatestAssignmentForEachAssignee(@Param("application") OdApplication application);
}