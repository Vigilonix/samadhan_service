package com.vigilonix.jaanch.repository;

import com.vigilonix.jaanch.aop.Timed;
import com.vigilonix.jaanch.enums.KandTag;
import com.vigilonix.jaanch.model.Kand;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository

public class KandRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Timed
    public List<Kand> findByPrefixNameAndGeoNodeIn(long startEpoch, long endEpoch, List<KandTag> tags, int limit, int offset, List<UUID> geoHierarchyNodeUuids) {
        // Using triple-quoted string for the SQL query
        String sqlQuery = """
        SELECT * FROM kand k
        WHERE k.incident_epoch BETWEEN :startEpoch AND :endEpoch
        AND k.target_geo_hierarchy_node_uuid IN :geoHierarchyNodeUuids
        ORDER BY k.created_at DESC
        LIMIT :limit OFFSET :offset
    """;

        // Create the native query
        Query query = entityManager.createNativeQuery(sqlQuery, Kand.class);
        query.setParameter("startEpoch", startEpoch);
        query.setParameter("endEpoch", endEpoch);

        // Convert List<KandTag> to an array of Strings for JSONB ?| operation

        query.setParameter("limit", limit);
        query.setParameter("offset", offset);
        query.setParameter("geoHierarchyNodeUuids", geoHierarchyNodeUuids);

        // Execute and return the result list
        return query.getResultList();
    }



}
