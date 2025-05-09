package com.vigilonix.samadhan.repository;

import com.vigilonix.samadhan.aop.Timed;
import com.vigilonix.samadhan.enums.KandTag;
import com.vigilonix.samadhan.model.Kand;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository

public class KandRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Timed
    public List<Kand> getKandListByFilter(long startEpoch, long endEpoch, Set<KandTag> tags, int limit, int offset, List<UUID> geoHierarchyNodeUuids) {
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
        AND k.incident_epoch BETWEEN :startEpoch AND :endEpoch
        AND k.target_geo_hierarchy_node_uuid IN (:geoHierarchyNodeUuids)
        ORDER BY k.incident_epoch DESC
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
                        k.incident_epoch BETWEEN :startEpoch AND :endEpoch
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
                    WHERE tag in ("""+tagsParam+"""
                    )
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

    @Timed
    public List<Object[]> findAggregatedByHourAndTag(long startEpoch, long endEpoch, Set<KandTag> tags, List<UUID> geoHierarchyNodeUuids) {
        String tagsParam = tags.stream()
                .map(tag -> "'" + tag + "'")
                .collect(Collectors.joining(", "));

        String sqlQuery = """
            WITH filter_kand AS (
                SELECT
                    uuid,
                    tags,
                    EXTRACT(HOUR FROM TO_TIMESTAMP(k.incident_epoch / 1000)) AS hour_of_day
                FROM
                    kand k
                WHERE 
                    k.incident_epoch BETWEEN :startEpoch AND :endEpoch
                    AND k.target_geo_hierarchy_node_uuid IN (:geoHierarchyNodeUuids)
            ),
            tag_kand AS (
                SELECT
                    uuid,
                    jsonb_array_elements_text(tags) AS tag,
                    hour_of_day
                FROM
                    filter_kand k
                WHERE
                    k.tags \\?\\?| array[""" + tagsParam + """
                                ]
                )
                SELECT
                    hour_of_day,
                    COALESCE(tag, 'ALL') AS tag,
                    COUNT(*) AS occurrences
                FROM (
                    SELECT
                        hour_of_day,
                        tag
                    FROM tag_kand
                    WHERE tag in ("""+tagsParam+"""
                            )
                    UNION ALL
                    SELECT
                        hour_of_day,
                        NULL AS tag
                    FROM
                        filter_kand
                ) AS combined
                GROUP BY hour_of_day, tag
                """;

        // Create the native query
        Query query = entityManager.createNativeQuery(sqlQuery);

        // Set the parameters
        query.setParameter("startEpoch", startEpoch);
        query.setParameter("endEpoch", endEpoch);
        query.setParameter("geoHierarchyNodeUuids", geoHierarchyNodeUuids);

        // Execute and return the result list as Object arrays (hour_of_day, tag, occurrences)
        return query.getResultList();
    }


    public List<Object[]> findCountAggregateByTag(long startEpoch, long endEpoch, Set<KandTag> tags, List<UUID> geoHierarchyNodeUuids) {
        // Query the aggregated data directly from the database
        String tagsParam = tags.stream()
                .map(tag -> "'" + tag + "'")
                .collect(Collectors.joining(", "));

        String sqlQuery = """
            WITH filter_kand AS (
                SELECT
                    uuid,
                    tags
                FROM
                    kand k
                WHERE 
                    k.incident_epoch BETWEEN :startEpoch AND :endEpoch
                    AND k.target_geo_hierarchy_node_uuid IN (:geoHierarchyNodeUuids)
            ),
            tag_kand AS (
                SELECT
                    uuid,
                    jsonb_array_elements_text(tags) AS tag
                FROM
                    filter_kand k
                WHERE
                    k.tags \\?\\?| array[""" + tagsParam + """
                                ]
                )
                SELECT
                    tag,
                    COUNT(*) AS occurrences
                FROM tag_kand 
                WHERE tag in ("""+tagsParam+"""
                       )
                GROUP BY tag
                """;

        // Create the native query
        Query query = entityManager.createNativeQuery(sqlQuery);

        // Set the parameters
        query.setParameter("startEpoch", startEpoch);
        query.setParameter("endEpoch", endEpoch);
        query.setParameter("geoHierarchyNodeUuids", geoHierarchyNodeUuids);

        // Execute the query and get the results
        List<Object[]> results = query.getResultList();
        return results;
    }

    public List<Object[]> findCountAggregateByGeoFence(long startEpoch, long endEpoch, Set<KandTag> tags, List<UUID> geoHierarchyNodeUuids) {
        // Query the aggregated data directly from the database
        String tagsParam = tags.stream()
                .map(tag -> "'" + tag + "'")
                .collect(Collectors.joining(", "));

        String sqlQuery = """
            WITH filter_kand AS (
                SELECT
                    uuid,
                    target_geo_hierarchy_node_uuid,
                    tags
                FROM
                    kand k
                WHERE 
                    k.incident_epoch BETWEEN :startEpoch AND :endEpoch
                    AND k.target_geo_hierarchy_node_uuid IN (:geoHierarchyNodeUuids)
            ),
            tag_kand AS (
                SELECT
                    uuid,
                    target_geo_hierarchy_node_uuid,
                    jsonb_array_elements_text(tags) AS tag
                FROM
                    filter_kand k
                WHERE
                    k.tags \\?\\?| array[""" + tagsParam + """
                                ]
                )
                SELECT
                    target_geo_hierarchy_node_uuid,
                    COUNT(DISTINCT(uuid)) AS occurrences
                FROM
                    tag_kand
                WHERE tag in ("""+tagsParam+"""
                                       )
                GROUP BY 1 
                """;

        // Create the native query
        Query query = entityManager.createNativeQuery(sqlQuery);

        // Set the parameters
        query.setParameter("startEpoch", startEpoch);
        query.setParameter("endEpoch", endEpoch);
        query.setParameter("geoHierarchyNodeUuids", geoHierarchyNodeUuids);

        // Execute the query and get the results
        List<Object[]> results = query.getResultList();
        return results;
    }
}
