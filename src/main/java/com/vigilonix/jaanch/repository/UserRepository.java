package com.vigilonix.jaanch.repository;

import com.vigilonix.jaanch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUuid(UUID uuid);

    User findByEmail(String email);

    List<User> findByIdIn(List<Long> ids);

    User findByUsername(String username);

    @Query(value = "SELECT * FROM users u WHERE u.name LIKE :prefixName% AND EXISTS (SELECT 1 FROM jsonb_each_text(u.post_geo_hierarchy_node_uuid_map) as elem WHERE elem.value::uuid IN :geoNodes)", nativeQuery = true)
    List<User> findByPrefixNameAndGeoNodeIn(@Param("prefixName") String prefixName, @Param("geoNodes") List<UUID> geoNodes);

    List<User> findByNameStartingWith(String prefixName);
}
