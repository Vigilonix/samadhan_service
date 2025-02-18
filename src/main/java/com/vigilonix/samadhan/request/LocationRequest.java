package com.vigilonix.samadhan.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LocationRequest {
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;
    @JsonProperty("location_time_in_ms")
    private Long locationTimeInMillis;
}
