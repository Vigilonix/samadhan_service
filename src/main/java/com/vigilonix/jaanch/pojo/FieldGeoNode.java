package com.vigilonix.jaanch.pojo;

import com.vigilonix.jaanch.enums.GeoHierarchyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class FieldGeoNode {
    private String name;
    private GeoHierarchyType type;
    private UUID uuid;
    List<LatLong> geofence;
    List<FieldGeoNode> children;
}
