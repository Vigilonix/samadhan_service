package com.vigilonix.jaanch.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DeviceTokenRequest {
    @NotEmpty
    @JsonProperty("device_token")
    private String deviceToken;
}
