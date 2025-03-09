package com.vigilonix.samadhan.repository;

import com.vigilonix.samadhan.aop.Timed;
import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.model.OdApplicationAssignment;
import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.pojo.OdApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OdApplicationAssignmentRepository extends JpaRepository<OdApplicationAssignment, Long> {

    @Timed
    OdApplication findByUuid(UUID uuid);

    @Timed
    List<OdApplicationAssignment> findByApplication(OdApplication application);

}