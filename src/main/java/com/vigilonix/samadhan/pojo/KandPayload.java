package com.vigilonix.samadhan.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vigilonix.samadhan.enums.KandTag;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KandPayload {
    private UUID uuid;

    private String firNo;

    private Double lat;

    private Double lang;

    private List<KandTag> tags;

    private UUID targetGeoHierarchyNodeUuid;

    private List<Section> sections;

    private List<String> sectionPayload;

    private Boolean isBns;

    private Long createdAt;
    private Long modifiedAt;

    private UUID updatedByUserUuid;

    private List<Person> victims;

    private List<Person> informants;

    private String sourceGeoHierarchyNodeUuid;

    private String firFilePath;

    private List<String> mediaPaths;

    private Long incidentEpoch;
}
