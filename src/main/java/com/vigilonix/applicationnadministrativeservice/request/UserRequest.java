package com.vigilonix.applicationnadministrativeservice.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vigilonix.applicationnadministrativeservice.enums.Post;
import com.vigilonix.applicationnadministrativeservice.enums.Rank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequest {
    private String uuid;
    @NotEmpty
    private String name;
    @ToString.Exclude
    private String password;
    @NotEmpty
    private String email;
    @NotEmpty
    private String username;
    private Long lastLive;
    @NotEmpty
    private Rank rank;
    @NotEmpty
    private String phoneNumber;
    private String firebaseDeviceToken;
    private Double latitude;
    private Double longitude;

    @JsonProperty("location_range")
    private Integer locationRangeInMeters;
    @JsonProperty("post_field_map")
    private Map<Post, List<UUID>> postGeoHierarchyNodeUuidMap;
}
