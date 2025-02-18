package com.vigilonix.samadhan.service;

import com.google.common.collect.Sets;
import com.vigilonix.samadhan.enums.KandTag;
import com.vigilonix.samadhan.enums.Post;
import com.vigilonix.samadhan.enums.ValidationErrorEnum;
import com.vigilonix.samadhan.exception.ValidationRuntimeException;
import com.vigilonix.samadhan.model.Kand;
import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.pojo.*;
import com.vigilonix.samadhan.repository.KandRepository;
import com.vigilonix.samadhan.repository.KandRepositoryCustom;
import com.vigilonix.samadhan.transformer.KandTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Transactional
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KandService {
    private final KandTransformer kandTransformer;
    private final KandRepository kandRepository;
    private final GeoHierarchyService geoHierarchyService;
    private final KandRepositoryCustom kandRepositoryCustom;

    // Create a new Kand
    public KandPayload createKand(KandPayload kandPayload, User principal, List<UUID> geoHierarchyNodeUuids) {
        Map<Post, List<UUID>> postGeoNodeMap = geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        Kand kand = Kand.builder()
                .uuid(UUID.randomUUID())
                .firNo(kandPayload.getFirNo())
                .lat(kandPayload.getLat())
                .lang(kandPayload.getLang())
                .tags(kandPayload.getTags())
                .targetGeoHierarchyNodeUuid(Objects.isNull(kandPayload.getTargetGeoHierarchyNodeUuid())? geoHierarchyService.getHighestPostNode(postGeoNodeMap).getUuid() : kandPayload.getTargetGeoHierarchyNodeUuid())
                .sections(kandPayload.getSections())
                .sectionPayload(kandPayload.getSectionPayload())
                .isBns(kandPayload.getIsBns())
                .createdAt(System.currentTimeMillis())
                .modifiedAt(System.currentTimeMillis())
                .updatedBy(principal)
                .victims(kandPayload.getVictims())
                .informants(kandPayload.getInformants())
                .sourceGeoHierarchyNodeUuid(kandPayload.getSourceGeoHierarchyNodeUuid())
                .firFilePath(kandPayload.getFirFilePath())
                .mediaPaths(kandPayload.getMediaPaths())
                .incidentEpoch(kandPayload.getIncidentEpoch())
                .build();

        // Save the new Kand entity
        Kand savedKand = kandRepository.save(kand);

        // Transform and return the saved entity as KandPayload
        return kandTransformer.transform(savedKand);
    }

    // Update an existing Kand
    public KandPayload updateKand(UUID kandUuid, KandPayload kandPayload, User principal) {
        // Find the existing Kand entity
        Kand existingKand = kandRepository.findByUuid(kandUuid);

        // Update the fields of the existing Kand entity
        existingKand.setFirNo(kandPayload.getFirNo());
        existingKand.setLat(kandPayload.getLat());
        existingKand.setLang(kandPayload.getLang());
        existingKand.setTags(kandPayload.getTags());
        existingKand.setTargetGeoHierarchyNodeUuid(kandPayload.getTargetGeoHierarchyNodeUuid());
        existingKand.setSections(kandPayload.getSections());
        existingKand.setSectionPayload(kandPayload.getSectionPayload());
        existingKand.setIsBns(kandPayload.getIsBns());
        existingKand.setModifiedAt(System.currentTimeMillis());
        existingKand.setUpdatedBy(principal);
        existingKand.setVictims(kandPayload.getVictims());
        existingKand.setInformants(kandPayload.getInformants());
        existingKand.setSourceGeoHierarchyNodeUuid(kandPayload.getSourceGeoHierarchyNodeUuid());
        existingKand.setFirFilePath(kandPayload.getFirFilePath());
        existingKand.setMediaPaths(kandPayload.getMediaPaths());
        existingKand.setIncidentEpoch(kandPayload.getIncidentEpoch());
        Kand updatedKand = kandRepository.save(existingKand);
        return kandTransformer.transform(updatedKand);
    }

    // Retrieve a single Kand by UUID
    public KandPayload getKand(UUID kandUuid, User principal) {
        Kand kand = kandRepository.findByUuid(kandUuid);
        if(kand == null) throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_UUID));
        return kandTransformer.transform(kand);
    }

    // Retrieve a list of Kand entities based on GeoHierarchy nodes
    public List<KandPayload> getKandList(User principal, List<UUID> geoHierarchyNodeUuids) {
        Map<Post, List<UUID>> postGeoNodeMap = geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        List<UUID> geoNodes= geoHierarchyService.getAllLevelNodesOfAuthorityPost(postGeoNodeMap);
        // Retrieve Kand entities based on GeoHierarchy nodes
        List<Kand> kandList = kandRepository.findByTargetGeoHierarchyNodeUuidIn(geoNodes);

        // Transform the list of Kand entities to KandPayloads
        return kandList.stream()
                .map(kandTransformer::transform)
                .collect(Collectors.toList());
    }

    public List<KandPayload> getKandFilterList(User principal, List<UUID> geoHierarchyNodeUuids, KandFilter kandFilter) {
        Map<Post, List<UUID>> postGeoNodeMap = geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        List<UUID> allGeoHierarchyUuids= geoHierarchyService.getAllLevelNodes(postGeoNodeMap);
        Set<KandTag> tags = Objects.isNull(kandFilter.getKandTags()) ? Sets.newHashSet(KandTag.values()) : Sets.newHashSet(kandFilter.getKandTags());
        tags.add(KandTag.ALL);

        return kandRepositoryCustom.getKandListByFilter(
                        Objects.isNull(kandFilter.getStartEpoch()) ? System.currentTimeMillis() : kandFilter.getStartEpoch(),
                        Objects.isNull(kandFilter.getEndEpoch()) ? System.currentTimeMillis() : kandFilter.getEndEpoch(),
                        tags,
                        Objects.isNull(kandFilter.getLimit()) ? 1000 : kandFilter.getLimit(),
                        Objects.isNull(kandFilter.getOffset()) ? 0 : kandFilter.getOffset(),
                        allGeoHierarchyUuids)
                .stream().map(kandTransformer::transform)
                .collect(Collectors.toList());
    }

    public ChartData getKandWeekDayTrend(User principal, List<UUID> geoHierarchyNodeUuids, KandFilter kandFilter) {
        // Resolve the GeoHierarchy nodes based on user and geoHierarchyNodeUuids
        Map<Post, List<UUID>> postGeoNodeMap = geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        List<UUID> allGeoHierarchyUuids = geoHierarchyService.getAllLevelNodes(postGeoNodeMap);
        Set<KandTag> tags = Objects.isNull(kandFilter.getKandTags()) ? Sets.newHashSet(KandTag.values()) : Sets.newHashSet(kandFilter.getKandTags());
        tags.add(KandTag.ALL);

        // Query the aggregated data directly from the database
        List<Object[]> results = kandRepositoryCustom.findAggregatedByDayOfWeekAndTag(
                Objects.isNull(kandFilter.getStartEpoch()) ? System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000 : kandFilter.getStartEpoch(),
                Objects.isNull(kandFilter.getEndEpoch()) ? System.currentTimeMillis() : kandFilter.getEndEpoch(),
                tags,
                allGeoHierarchyUuids
        );

        // Initialize a map to store trend data for each KandTag
        Map<KandTag, int[]> tagDataMap = new HashMap<>();
        for (KandTag tag : tags) {
            tagDataMap.put(tag, new int[7]); // Initialize an array of size 7 (Monday to Sunday) for each KandTag
        }

        // Process the query results to fill in the trend data arrays
        for (Object[] result : results) {
            int dayOfWeek = Objects.isNull(result[0]) ? 0 : (((BigDecimal) result[0]).intValue()); // Extract day_of_week (0 = Sunday, 6 = Saturday)
            String tagString = (String) result[1];
            int occurrences = ((Long) result[2]).intValue();

            // Find the KandTag corresponding to the tag string
            for (KandTag tag : tags) {
                if (tagString.contains(tag.name())) {
                    tagDataMap.get(tag)[dayOfWeek] += occurrences; // Increment the appropriate day for the tag
                }
            }
        }

        // Convert the map entries into Series for the chart
        List<Series> seriesList = tagDataMap.entrySet().stream()
                .map(entry -> Series.builder()
                        .id(entry.getKey().name().toLowerCase())
                        .label(entry.getKey().getLabel()) // Assuming KandTag has a getName() method
                        .data(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        // Create the chart data object with series
        ChartData chartData = ChartData.builder()
                .xLabels(Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")) // 0 = Sunday, 6 = Saturday
                .series(seriesList)
                .build();

        return chartData;
    }

    public ChartData getKandHourTrend(User principal, List<UUID> geoHierarchyNodeUuids, KandFilter kandFilter) {
        Map<Post, List<UUID>> postGeoNodeMap = geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        List<UUID> allGeoHierarchyUuids = geoHierarchyService.getAllLevelNodes(postGeoNodeMap);
        Set<KandTag> tags = Objects.isNull(kandFilter.getKandTags()) ? Sets.newHashSet(KandTag.values()) : Sets.newHashSet(kandFilter.getKandTags());
        tags.add(KandTag.ALL);

        // Query the aggregated data directly from the database for hourly trend
        List<Object[]> results = kandRepositoryCustom.findAggregatedByHourAndTag(
                Objects.isNull(kandFilter.getStartEpoch()) ? System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000 : kandFilter.getStartEpoch(),
                Objects.isNull(kandFilter.getEndEpoch()) ? System.currentTimeMillis() : kandFilter.getEndEpoch(),
                tags,
                allGeoHierarchyUuids
        );

        // Initialize a map to store trend data for each KandTag
        TreeMap<KandTag, int[]> tagDataMap = new TreeMap<>((a,b)-> Integer.compare(a.getOrder(), b.getOrder()));
        for (KandTag tag : tags) {
            tagDataMap.put(tag, new int[24]); // Initialize an array of size 24 (0 to 23 hours) for each KandTag
        }

        // Process the query results to fill in the trend data arrays
        for (Object[] result : results) {
            int hourOfDay = Objects.isNull(result[0]) ? 0 : (((BigDecimal) result[0]).intValue()); // Extract hour_of_day (0 = Midnight, 23 = 11 PM)
            String tagString = (String) result[1];
            int occurrences = ((Long) result[2]).intValue();

            // Find the KandTag corresponding to the tag string
            for (KandTag tag : tags) {
                if (tagString.contains(tag.name())) {
                    tagDataMap.get(tag)[hourOfDay] += occurrences; // Increment the appropriate hour for the tag
                }
            }
        }

        // Convert the map entries into Series for the chart
        List<Series> seriesList = tagDataMap.entrySet().stream()
                .map(entry -> Series.builder()
                        .id(entry.getKey().name().toLowerCase())
                        .label(entry.getKey().getLabel()) // Assuming KandTag has a getName() method
                        .data(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        // Create the chart data object with series
        ChartData chartData = ChartData.builder()
                .xLabels(Arrays.asList(
                        "12 AM", "1 AM", "2 AM", "3 AM", "4 AM", "5 AM", "6 AM", "7 AM", "8 AM", "9 AM", "10 AM", "11 AM",
                        "12 PM", "1 PM", "2 PM", "3 PM", "4 PM", "5 PM", "6 PM", "7 PM", "8 PM", "9 PM", "10 PM", "11 PM")) // Labels for hours of the day
                .series(seriesList)
                .build();

        return chartData;
    }

    public List<GroupData> getTagCounters(User principal, List<UUID> geoHierarchyNodeUuids, KandFilter kandFilter) {
        Map<Post, List<UUID>> postGeoNodeMap = geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        List<UUID> allGeoHierarchyUuids = geoHierarchyService.getAllLevelNodes(postGeoNodeMap);
        Set<KandTag> tags = Objects.isNull(kandFilter.getKandTags()) ? Sets.newHashSet(KandTag.values()) : Sets.newHashSet(kandFilter.getKandTags());
        tags.add(KandTag.ALL);

        long startEpoch = Objects.isNull(kandFilter.getStartEpoch()) ? System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000 : kandFilter.getStartEpoch();
        long endEpoch = Objects.isNull(kandFilter.getEndEpoch()) ? System.currentTimeMillis() : kandFilter.getEndEpoch();

        // Fetch tag counters from DAO layer
        List<Object[]> results = kandRepositoryCustom.findCountAggregateByTag(startEpoch, endEpoch, tags, allGeoHierarchyUuids);


        // Convert the query results into GroupData
        List<GroupData> groupDataList = results.stream()
                .map(result -> new GroupData(KandTag.valueOf((String)result[0]).getLabel(), ((Long) result[1]).intValue())) // (tag, occurrences)
                .collect(Collectors.toList());

        return groupDataList; 
    }

    public Map<UUID, Integer> geoFenceCounter(User principal, List<UUID> geoHierarchyNodeUuids, KandFilter kandFilter) {
        Map<Post, List<UUID>> postGeoNodeMap = geoHierarchyService.resolveGeoHierarchyNodes(principal.getPostGeoHierarchyNodeUuidMap(), geoHierarchyNodeUuids);
        List<UUID> allGeoHierarchyUuids = geoHierarchyService.getAllLevelNodes(postGeoNodeMap);
        Set<KandTag> tags = Objects.isNull(kandFilter.getKandTags()) ? Sets.newHashSet(KandTag.values()) : Sets.newHashSet(kandFilter.getKandTags());
        tags.add(KandTag.ALL);

        long startEpoch = Objects.isNull(kandFilter.getStartEpoch()) ? System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000 : kandFilter.getStartEpoch();
        long endEpoch = Objects.isNull(kandFilter.getEndEpoch()) ? System.currentTimeMillis() : kandFilter.getEndEpoch();

        // Fetch tag counters from DAO layer
        List<Object[]> results = kandRepositoryCustom.findCountAggregateByGeoFence(startEpoch, endEpoch, tags, allGeoHierarchyUuids);
        Map<UUID,Integer> geoCounterMap = new HashMap<>();
        for(Object[] result: results) {
            int occurrences= Objects.isNull(result[1]) ? 0 : (((Long) result[1]).intValue());
            UUID geoHierarchyNodeUuid= (UUID) result[0];
            geoCounterMap.put(geoHierarchyNodeUuid, occurrences);
        }
        for(Map.Entry<Post, List<UUID>> entry: principal.getPostGeoHierarchyNodeUuidMap().entrySet()) {

            for(UUID geoHierarchyNodeUuid: entry.getValue()) {
                recursePopulateKandCounter(geoCounterMap, geoHierarchyNodeUuid);
            }
        }
        return geoCounterMap;
    }

    private int recursePopulateKandCounter(Map<UUID, Integer> geoCounterMap, UUID geoHierarchyNodeUuid) {
        GeoHierarchyNode geoHierarchyNode = geoHierarchyService.getNodeById(geoHierarchyNodeUuid);
        if(CollectionUtils.isEmpty(geoHierarchyNode.getChildren())) {
            geoCounterMap.putIfAbsent(geoHierarchyNodeUuid,0);
            return geoCounterMap.get(geoHierarchyNodeUuid);
        }
        int val=0;
        for(GeoHierarchyNode child: geoHierarchyNode.getChildren()) {
            val+= recursePopulateKandCounter(geoCounterMap, child.getUuid());
        }
        //no need to count Kand which is tied to higher geo fence than thana as kand is always tied to thana and no higher hierarchy
        geoCounterMap.put(geoHierarchyNodeUuid, val);
        return val;
    }
}
