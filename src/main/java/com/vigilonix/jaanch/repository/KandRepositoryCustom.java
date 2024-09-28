package com.vigilonix.jaanch.repository;

import com.vigilonix.jaanch.aop.Timed;
import com.vigilonix.jaanch.enums.KandTag;
import com.vigilonix.jaanch.model.Kand;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository

public class KandRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Timed
    public List<Kand> findByPrefixNameAndGeoNodeIn(long startEpoch, long endEpoch, List<KandTag> tags, int limit, int offset, List<UUID> geoHierarchyNodeUuids) {
        // Assuming tagsParam is a list or array of strings
        String tagsParam = tags.stream()
                .map(tag -> "'" + tag + "'")
                .collect(Collectors.joining(", "));

        String sqlQuery = """
        SELECT k.*
        FROM kand k
        WHERE 1=1
        AND k.tags \\?\\?| array[""" + tagsParam + """
        ]
        ORDER BY k.created_at DESC
    """;

// Create the native query
        Query query = entityManager.createNativeQuery(sqlQuery, Kand.class);

// Execute and return the result list
        return query.getResultList();

    }




}
