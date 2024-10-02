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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository

public class KandRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Timed
    public List<Kand> findByPrefixNameAndGeoNodeIn(long startEpoch, long endEpoch, List<KandTag> tags, int limit, int offset, List<UUID> geoHierarchyNodeUuids) {
        // Format the tags parameter as a string for SQL
        String tagsParam = tags.stream()
                .map(tag -> "'" + tag + "'")
                .collect(Collectors.joining(", "));

        // Create the SQL query with all parameters
        String sqlQuery = """
        SELECT k.*
        FROM kand k
        WHERE 1=1
        AND k.tags \\?\\?| array[""" + tagsParam + """
        ]
        AND k.created_at BETWEEN :startEpoch AND :endEpoch
        AND k.target_geo_hierarchy_node_uuid IN (:geoHierarchyNodeUuids)
        ORDER BY k.created_at DESC
        LIMIT :limit OFFSET :offset
    """;

        // Create the native query
        Query query = entityManager.createNativeQuery(sqlQuery, Kand.class);

        // Set the parameters
        query.setParameter("startEpoch", startEpoch);
        query.setParameter("endEpoch", endEpoch);
        query.setParameter("limit", limit);
        query.setParameter("offset", offset);
        query.setParameter("geoHierarchyNodeUuids", geoHierarchyNodeUuids);

        // Execute and return the result list
        return query.getResultList();
    }

    @Timed
    public List<Object[]> findAggregatedByDayOfWeekAndTag(long startEpoch, long endEpoch, Set<KandTag> tags, List<UUID> geoHierarchyNodeUuids) {
        String tagsParam = tags.stream()
                .map(tag -> "'" + tag + "'")
                .collect(Collectors.joining(", "));
        String sqlQuery= """
                WITH filter_kand AS (
                    SELECT
                        uuid,
                        tags,
                        EXTRACT(DOW FROM TO_TIMESTAMP(k.incident_epoch / 1000)) AS day_of_week
                    FROM
                        kand k
                    WHERE 
                        k.created_at BETWEEN :startEpoch AND :endEpoch
                        AND k.target_geo_hierarchy_node_uuid IN (:geoHierarchyNodeUuids)
                ),
                tag_kand AS (
                    SELECT
                        uuid,
                        jsonb_array_elements_text(tags) AS tag,
                        day_of_week
                    FROM
                        filter_kand k
                    WHERE
                        k.tags \\?\\?| array[""" + tagsParam + """
                                ]
                )
                SELECT
                    day_of_week,
                    COALESCE(tag, 'ALL') AS tag,
                    COUNT(*) AS occurrences
                FROM (
                    SELECT
                        day_of_week,
                        tag
                    FROM tag_kand
                    UNION ALL
                    SELECT
                        day_of_week,
                        NULL AS tag
                    FROM
                        filter_kand
                ) AS combined
                GROUP BY day_of_week, tag
                """;

        // Create the native query
        Query query = entityManager.createNativeQuery(sqlQuery);

        // Set the parameters
        query.setParameter("startEpoch", startEpoch);
        query.setParameter("endEpoch", endEpoch);
        query.setParameter("geoHierarchyNodeUuids", geoHierarchyNodeUuids);

        // Execute and return the result list as Object arrays (day_of_week, tags, occurrences)
        return query.getResultList();
    }




}
