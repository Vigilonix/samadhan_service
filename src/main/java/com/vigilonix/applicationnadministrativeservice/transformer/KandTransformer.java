package com.vigilonix.applicationnadministrativeservice.transformer;

import com.vigilonix.applicationnadministrativeservice.model.Kand;
import com.vigilonix.applicationnadministrativeservice.pojo.KandPayload;
import com.vigilonix.applicationnadministrativeservice.service.GeoHierarchyService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class KandTransformer implements Transformer<Kand, KandPayload> {
    private final GeoHierarchyService geoHierarchyService;

    @Override
    public KandPayload transform(Kand kand) {
        return KandPayload.builder()
                .uuid(kand.getUuid())
                .firNo(kand.getFirNo())
                .lat(kand.getLat())
                .lang(kand.getLang())
                .tags(kand.getTags())
                .targetGeoHierarchyNodeUuid(kand.getTargetGeoHierarchyNodeUuid())
                .sections(kand.getSections())
                .sectionPayload(kand.getSectionPayload())
                .isBns(kand.getIsBns())
                .createdAt(kand.getCreatedAt())
                .modifiedAt(kand.getModifiedAt())
                .updatedByUserUuid(kand.getUpdatedBy().getUuid())
                .victims(kand.getVictims())
                .informants(kand.getInformants())
                .sourceGeoHierarchyNodeUuid(kand.getSourceGeoHierarchyNodeUuid())
                .firFilePath(kand.getFirFilePath())
                .mediaPaths(kand.getMediaPaths())
                .incidentEpoch(kand.getIncidentEpoch())
                .build();
    }
}
