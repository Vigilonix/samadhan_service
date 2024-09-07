package com.vigilonix.jaanch.repository;

import com.vigilonix.jaanch.aop.Timed;
import com.vigilonix.jaanch.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository

public class UserRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Timed
    public List<User> findByPrefixNameAndGeoNodeIn(String prefixName, List<UUID> geoNodes) {
        // Convert the List<UUID> to a comma-separated string
        String geoNodesString = geoNodes.stream()
                .map(UUID::toString)
                .map(uuid -> "'" + uuid + "'")
                .collect(Collectors.joining(","));

        String sqlQuery = "SELECT * FROM users u WHERE lower(u.name) LIKE :prefixName AND EXISTS (SELECT 1 FROM jsonb_each_text(u.post_geo_hierarchy_node_uuid_map) AS nodes(key, value) WHERE EXISTS (SELECT 1 FROM jsonb_array_elements_text(value::jsonb) AS elem WHERE elem::uuid IN ("+geoNodesString+")))";
        Query query = entityManager.createNativeQuery(sqlQuery, User.class);
        query.setParameter("prefixName", prefixName.toLowerCase() + "%");  // Adding '%' for the LIKE clause

        return query.getResultList();
    }

    @Timed
    public List<User> findAuthorityGeoHierarchyUser(UUID geoHierarchyNodeUuid) {
        String sqlQuery = """
        SELECT * 
        FROM users u 
        WHERE NOT jsonb_exists(u.post_geo_hierarchy_node_uuid_map, 'BEAT') 
        AND EXISTS ( 
            SELECT 1 
            FROM jsonb_each(u.post_geo_hierarchy_node_uuid_map::jsonb) AS post(post_key, post_value) 
            WHERE post.post_key != 'BEAT' 
            AND jsonb_exists(post.post_value, :uuid)
        )
    """;

        Query query = entityManager.createNativeQuery(sqlQuery, User.class);
        query.setParameter("uuid", geoHierarchyNodeUuid.toString());

        return query.getResultList();
    }



}
