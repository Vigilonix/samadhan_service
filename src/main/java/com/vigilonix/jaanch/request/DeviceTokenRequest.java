package com.vigilonix.jaanch.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DeviceTokenRequest {
    @NotEmpty
    @JsonProperty("device_token")
    private String deviceToken;
}
