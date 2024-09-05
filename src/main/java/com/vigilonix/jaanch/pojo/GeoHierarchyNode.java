package com.vigilonix.jaanch.pojo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vigilonix.jaanch.enums.GeoHierarchyType;
import lombok.*;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GeoHierarchyNode {
    private String name;
    private GeoHierarchyType type;
    private UUID uuid;
    private List<LatLong> geofence;
    private List<GeoHierarchyNode> children;
    private Boolean isTest;
    private Boolean isDemo;

}
