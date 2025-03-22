package com.vigilonix.samadhan.repository;

import com.vigilonix.samadhan.aop.Timed;
import com.vigilonix.samadhan.enums.Post;
import com.vigilonix.samadhan.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
