package com.vigilonix.jaanch.repository;

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

    public List<User> findByPrefixNameAndGeoNodeIn(String prefixName, List<UUID> geoNodes) {
        // Convert the List<UUID> to a comma-separated string
        String geoNodesString = geoNodes.stream()
                .map(UUID::toString)
                .map(uuid -> "'" + uuid + "'")
                .collect(Collectors.joining(","));

        // Construct the native SQL query
        String sqlQuery = "SELECT * FROM users u WHERE lower(u.name) LIKE :prefixName AND EXISTS (SELECT 1 FROM jsonb_each_text(u.post_geo_hierarchy_node_uuid_map) AS nodes(key, value) WHERE EXISTS (SELECT 1 FROM jsonb_array_elements_text(value::jsonb) AS elem WHERE elem::uuid IN ("+geoNodesString+")))";
//        String sqlQuery = "SELECT * FROM users u WHERE EXISTS (SELECT 1 FROM jsonb_each(post_geo_hierarchy_node_uuid_map) as nodes(key, value) WHERE value @> to_jsonb(ARRAY["+geoNodesString+"]::text[])::jsonb)";


//        String sqlQuery = "SELECT * FROM users u " +
//                "WHERE lower(u.name) LIKE :prefixName " +
//                "AND EXISTS (" +
//                "    SELECT 1 FROM jsonb_each_text(u.post_geo_hierarchy_node_uuid_map) AS nodes(key, value) " +
//                "    WHERE value::uuid IN (" + geoNodesString + ")" +
//                ")";

        // Create and execute the query
        Query query = entityManager.createNativeQuery(sqlQuery, User.class);
        query.setParameter("prefixName", prefixName.toLowerCase() + "%");  // Adding '%' for the LIKE clause

        return query.getResultList();
    }
}
