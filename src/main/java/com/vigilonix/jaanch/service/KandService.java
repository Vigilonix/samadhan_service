package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.enums.Post;
import com.vigilonix.jaanch.model.Kand;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.KandPayload;
import com.vigilonix.jaanch.repository.KandRepository;
import com.vigilonix.jaanch.transformer.KandTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
                .incidentTime(kandPayload.getIncidentTime())
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
        existingKand.setIncidentTime(kandPayload.getIncidentTime());
        Kand updatedKand = kandRepository.save(existingKand);
        return kandTransformer.transform(updatedKand);
    }

    // Retrieve a single Kand by UUID
    public KandPayload getKand(UUID kandUuid, User principal) {
        Kand kand = kandRepository.findByUuid(kandUuid);
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
}
