package com.vigilonix.jaanch.request;

import com.dt.beyond.enums.Country;
import com.dt.beyond.enums.State;
import com.dt.beyond.model.*;
import com.dt.beyond.request.UserMediaResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
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
}
