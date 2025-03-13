package com.vigilonix.samadhan.repository;

import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.model.OdApplicationAssignment;
import com.vigilonix.samadhan.model.OdApplicationAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OdApplicationAssignmentHistoryRepository extends JpaRepository<OdApplicationAssignmentHistory, UUID> {

    // Finds an assignment by its UUID
    List<OdApplicationAssignmentHistory> findByAssignmentUuid(UUID uuid);

}