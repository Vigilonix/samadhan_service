package com.vigilonix.jaanch.pojo;

import com.vigilonix.jaanch.enums.GeoHierarchyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class FieldGeoNode {
    private String name;
    private GeoHierarchyType type;
    private UUID uuid;
    List<LatLong> geofence;
    List<FieldGeoNode> children;
}
