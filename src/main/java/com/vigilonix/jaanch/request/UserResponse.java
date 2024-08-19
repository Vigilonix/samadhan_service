package com.vigilonix.jaanch.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vigilonix.jaanch.enums.Post;
import com.vigilonix.jaanch.enums.Rank;
import com.vigilonix.jaanch.enums.Role;
import com.vigilonix.jaanch.enums.State;
import com.vigilonix.jaanch.pojo.FieldGeoNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserResponse {
    private final UUID uuid;
    private final String name;

    @Setter
    private String username;
    @Setter
    private String email;
    @Setter
    @JsonProperty("created_on")
    private Long createdOn;
    private Long modifiedOn;
    private Long stateChangedOn;
    private Role role;
    private Rank rank;
    private State state;
    private String deviceToken;
    private String phoneNumber;
    private Long lastLive;
    private Long lastLocationUpdateTimeInMillis;
    private Double latitude;
    private Double longitude;
    @JsonProperty("post_field_map")
    private Map<Post, List<FieldGeoNode>> postFieldGeoNodeUuidMap;
    private Post highestPost;
}
