package com.vigilonix.samadhan.repository;

import com.vigilonix.samadhan.aop.Timed;
import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.service.GeoHierarchyService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private GeoHierarchyService geoHierarchyService;

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
    public List<User> findAuthorityGeoHierarchyUsers(UUID geoHierarchyNodeUuid) {
        List<String> postNames = geoHierarchyService.getAuthorityPosts().stream().map(Enum::name).collect(Collectors.toList());


        // Convert List<String> to PostgreSQL array string format
        String postNamesFormatted = postNames.stream()
                .map(name -> "'" + name + "'")
                .collect(Collectors.joining(", "));
        // Using the @> operator to check for the presence of a UUID in a JSONB array
        String sqlQuery = String.format("SELECT u.* FROM users u, jsonb_each(u.post_geo_hierarchy_node_uuid_map) AS kv " +
                "WHERE kv.key IN (%s) AND kv.value @> '\"%s\"'", postNamesFormatted, geoHierarchyNodeUuid);

        // Executing the query
        Query query = entityManager.createNativeQuery(sqlQuery, User.class);
        return query.getResultList();
    }

    public User findAuthorityGeoHierarchyUser(UUID geoHierarchyNodeUuid) {
        List<User> authUsers = findAuthorityGeoHierarchyUsers(geoHierarchyNodeUuid);
        if(CollectionUtils.isEmpty(authUsers)) return null;
        return authUsers.get(0);
    }
}
