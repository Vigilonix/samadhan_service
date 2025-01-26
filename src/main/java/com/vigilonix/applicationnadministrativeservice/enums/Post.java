package com.vigilonix.applicationnadministrativeservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Post {
    BEAT(100),
    ASSISTANT_SHO(200),
    SHO(300),
    SDPO(400),
    CITY_SP(500),
    SP(600);
    private final int level;
}
