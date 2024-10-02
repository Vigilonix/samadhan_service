package com.vigilonix.jaanch.service;

import com.google.common.collect.Sets;
import com.vigilonix.jaanch.enums.KandTag;
import com.vigilonix.jaanch.enums.Post;
import com.vigilonix.jaanch.enums.ValidationErrorEnum;
import com.vigilonix.jaanch.exception.ValidationRuntimeException;
import com.vigilonix.jaanch.model.Kand;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.*;
import com.vigilonix.jaanch.repository.KandRepository;
import com.vigilonix.jaanch.repository.KandRepositoryCustom;
import com.vigilonix.jaanch.transformer.KandTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return kandRepositoryCustom.findByPrefixNameAndGeoNodeIn(
                        Objects.isNull(kandFilter.getStartEpoch()) ? System.currentTimeMillis() : kandFilter.getStartEpoch(),
                        Objects.isNull(kandFilter.getEndEpoch()) ? System.currentTimeMillis() : kandFilter.getEndEpoch(),
                        Objects.isNull(kandFilter.getKandTags()) ? Arrays.asList(KandTag.values()) : kandFilter.getKandTags(),
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
            int dayOfWeek = Objects.isNull(result[0]) ? 0 : (((Double) result[0]).intValue()); // Extract day_of_week (0 = Sunday, 6 = Saturday)
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
                        .label(entry.getKey().getName()) // Assuming KandTag has a getName() method
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
        List<String> hourLabels = Arrays.asList(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
                "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24"
        );

        // Create the chartData object with hourly data for the crime types
        ChartData chartData = ChartData.builder()
                .xLabels(hourLabels)
                .series(Arrays.asList(
                        Series.builder()
                                .id("a")
                                .label("Chain Snatching")
                                .data(new int[]{1, 3, 2, 0, 5, 3, 4, 2, 1, 3, 1, 4, 5, 3, 6, 2, 1, 0, 1, 3, 2, 4, 5, 2}) // Sample hourly data for Chain Snatching
                                .build(),
                        Series.builder()
                                .id("b")
                                .label("Mobile Snatching")
                                .data(new int[]{0, 1, 1, 2, 2, 1, 0, 1, 3, 2, 1, 4, 3, 2, 3, 1, 1, 2, 1, 0, 1, 2, 1, 2}) // Sample hourly data for Mobile Snatching
                                .build(),
                        Series.builder()
                                .id("c")
                                .label("Two Wheeler Theft")
                                .data(new int[]{2, 2, 3, 1, 4, 2, 1, 0, 1, 2, 4, 3, 2, 1, 5, 3, 4, 2, 1, 2, 3, 1, 0, 1}) // Sample hourly data for Two Wheeler Theft
                                .build()
                ))
                .build();

        return chartData;
    }

    public List<GroupData> getTagCounters(User principal, List<UUID> geoHierarchyNodeUuids, KandFilter kandFilter) {
        List<GroupData> groupDataList = new ArrayList<>();
        Random random = new Random();

        // Iterate through KandTag enum values to generate sample GroupData
        for (KandTag kandTag : KandTag.values()) {
            int randomValue = random.nextInt(500); // Random value between 0 and 499
            groupDataList.add(new GroupData(kandTag.getName(), randomValue));
        }

        return groupDataList;
    }
}
