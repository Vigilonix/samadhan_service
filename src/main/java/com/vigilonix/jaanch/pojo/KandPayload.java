package com.vigilonix.jaanch.pojo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KandPayload {
    private UUID uuid;
    private Double lat;
    private Double lang;
    private List<String> tag;
    private UUID geoHierarchyNodeUuid;
    private Integer section;
    private String subSection;
}
