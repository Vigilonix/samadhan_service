package com.vigilonix.applicationnadministrativeservice.repository;

import com.vigilonix.applicationnadministrativeservice.aop.Timed;
import com.vigilonix.applicationnadministrativeservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Timed
    User findByUuid(UUID uuid);

    User findByEmail(String email);

    List<User> findByIdIn(List<Long> ids);

    @Timed
    User findByUsername(String username);

    List<User> findByNameStartingWith(String prefixName);

}
