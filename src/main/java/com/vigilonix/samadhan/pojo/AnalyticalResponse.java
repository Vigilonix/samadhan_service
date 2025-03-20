package com.vigilonix.samadhan.pojo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@AllArgsConstructor
@Getter
@Builder
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AnalyticalResponse {
    private final Map<OdApplicationStatus, Long> statusCountMap;
    private final Map<OdApplicationStatus, Long> selfStatusCountMap;
    private final GeoHierarchyAnalyticalResponse geoHierarchyAnalyticalResponse;
}
