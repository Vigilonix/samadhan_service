package com.vigilonix.jaanch.pojo;

import com.vigilonix.jaanch.enums.GeoHierarchyType;
import lombok.*;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@Builder
public class FieldGeoNode {
    private String name;
    private GeoHierarchyType type;
    private UUID uuid;
    List<LatLong> geofence;
    List<FieldGeoNode> children;
}
