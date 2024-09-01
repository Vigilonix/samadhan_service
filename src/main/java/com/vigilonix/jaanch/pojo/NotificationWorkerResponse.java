package com.vigilonix.jaanch.pojo;

import com.vigilonix.jaanch.enums.NotificationMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
@ToString
public class NotificationWorkerResponse {
    private final boolean success;
    private final String response;
}
