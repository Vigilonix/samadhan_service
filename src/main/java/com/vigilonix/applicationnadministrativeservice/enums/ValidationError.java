package com.vigilonix.applicationnadministrativeservice.enums;

import java.util.List;

public interface ValidationError {
    int getCode();

    String getMessageFormat();

    List<String> getAttributes();
}
