package com.vigilonix.jaanch.transformer;

import com.vigilonix.jaanch.model.Kand;
import com.vigilonix.jaanch.pojo.KandPayload;
import com.vigilonix.jaanch.service.GeoHierarchyService;
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
                .tag(kand.getTag())
                .targetGeoHierarchyNodeUuid(kand.getTargetGeoHierarchyNodeUuid())
                .sections(kand.getSections())
                .createdAt(kand.getCreatedAt())
                .modifiedAt(kand.getModifiedAt())
                .updatedByUserUuid(kand.getUpdatedBy().getUuid())
                .victims(kand.getVictims())
                .informants(kand.getInformants())
                .sourceGeoHierarchyNodeUuid(kand.getSourceGeoHierarchyNodeUuid())
                .firFilePath(kand.getFirFilePath())
                .mediaPaths(kand.getMediaPaths())
                .build();
    }
}
