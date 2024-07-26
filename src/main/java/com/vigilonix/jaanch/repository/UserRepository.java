package com.vigilonix.jaanch.repository;

import com.vigilonix.jaanch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUuid(UUID uuid);

    User findByEmail(String email);

    List<User> findByIdIn(List<Long> ids);

    User findByUsername(String username);


}
